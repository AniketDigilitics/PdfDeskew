package com.example.pdf_deskew.service;

import com.example.pdf_deskew.dto.PageTransformRequest;
import com.example.pdf_deskew.repository.JobRepository;
import com.example.pdf_deskew.ImageUtils;
import com.example.pdf_deskew.entity.JobEntity;
import com.example.pdf_deskew.entity.JobDocumentEntity;
import com.example.pdf_deskew.entity.PageEntity;
import com.example.pdf_deskew.repository.PageRepository;
import com.example.pdf_deskew.repository.JobDocumentRepository;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.graphics.image.JPEGFactory;
import org.apache.pdfbox.rendering.ImageType;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.bytedeco.javacpp.indexer.UByteRawIndexer;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import org.bytedeco.opencv.opencv_core.*;
import org.bytedeco.opencv.global.opencv_core;
import org.bytedeco.opencv.global.opencv_imgproc;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.Instant;
import java.util.Arrays;


import java.util.ArrayList;
import java.util.List;

@Service
public class PdfDeskewService {

    private static final int RENDER_DPI = 150;

    private final JobRepository jobRepository;
    private final PageRepository pageRepository;
    private final JobDocumentRepository jobDocumentRepository;

    static {
        org.bytedeco.javacpp.Loader.load(opencv_core.class);
    }

    public PdfDeskewService(
            JobRepository jobRepository,
            PageRepository pageRepository,
            JobDocumentRepository jobDocumentRepository
    ) {
        this.jobRepository = jobRepository;
        this.pageRepository = pageRepository;
        this.jobDocumentRepository = jobDocumentRepository;
    }

    // Java 17+ record for convenience
    public record Result(byte[] correctedPdf, List<Double> pageAngles) {}

    public Result process(byte[] pdfBytes) throws IOException {
        try (PDDocument doc = PDDocument.load(pdfBytes)) {

            PDFRenderer renderer = new PDFRenderer(doc);
            List<Double> angles = new ArrayList<>();

            try (PDDocument out = new PDDocument()) {
                for (int i = 0; i < doc.getNumberOfPages(); i++) {
                    PDPage srcPage = doc.getPage(i);

                    long start = System.nanoTime();
                    long t0 = System.nanoTime();
                    BufferedImage pageImage = renderer.renderImageWithDPI(i,300 , ImageType.RGB);

                    long t1 = System.nanoTime();
                    // --- Step 2: Detect angle ---
                    double angle = detectSkewAngle(pageImage);
                    long t2 = System.nanoTime();
                    angles.add(angle);


                    // --- Step 4: Rotate and write ---
                    BufferedImage rotated = rotateImage(pageImage, angle);
                    long t3 = System.nanoTime();
                    PDRectangle box = srcPage.getCropBox() != null ? srcPage.getCropBox() : srcPage.getMediaBox();
                    PDPage newPage = new PDPage(new PDRectangle(box.getWidth(), box.getHeight()));
                    out.addPage(newPage);

                    var pdImage = JPEGFactory.createFromImage(out, rotated, 0.85f);
                    try (PDPageContentStream cs = new PDPageContentStream(out, newPage)) {
                        cs.drawImage(pdImage, 0, 0,
                                newPage.getMediaBox().getWidth(),
                                newPage.getMediaBox().getHeight());
                    }
                    long t4 = System.nanoTime();

                    // --- Stage timings ---
                    double renderMs  = (t1 - t0) / 1_000_000.0;
                    double detectMs  = (t2 - t1) / 1_000_000.0;
                    double rotateMs  = (t3 - t2) / 1_000_000.0;
                    double writeMs   = (t4 - t3) / 1_000_000.0;
                    double totalMs   = (t4 - t0) / 1_000_000.0;

                    System.out.printf(
                            "Page %d timings -> Render: %.2f ms | Detect: %.2f ms | Rotate: %.2f ms | Write: %.2f ms | TOTAL: %.2f ms%n",
                            i + 1, renderMs, detectMs, rotateMs, writeMs, totalMs
                    );
                }

                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                out.save(baos);
                return new Result(baos.toByteArray(), angles);
            }
        }
    }

    @Transactional
    public void applyTransforms(String jobId, List<PageTransformRequest> transforms) {
        JobEntity job = jobRepository.findById(jobId)
                .orElseThrow(() -> new RuntimeException("Job not found: " + jobId));

        // Update applied angles in DB
        for (PageTransformRequest tr : transforms) {
            PageEntity page = pageRepository.findByJobIdAndPageIndex(jobId, tr.getPageIndex())
                    .orElseThrow(() -> new RuntimeException("Page not found: " + tr.getPageIndex()));
            page.setAppliedAngle(tr.getAppliedAngle());
            pageRepository.save(page);
        }

        // Regenerate corrected PDF physically rotated
        byte[] correctedPdf = reapplyTransforms(jobId);

        // Save corrected PDF in job_documents
        JobDocumentEntity doc = jobDocumentRepository.findById(jobId)
                .orElseThrow(() -> new RuntimeException("Job document not found for job: " + jobId));

        doc.setCorrectedPdf(correctedPdf);
        jobDocumentRepository.save(doc);

        // Update job status timestamp
        job.setUpdatedAt(Instant.now());
        jobRepository.save(job);
    }

    public byte[] reapplyTransforms(String jobId) {
        JobDocumentEntity docEntity = jobDocumentRepository.findById(jobId)
                .orElseThrow(() -> new RuntimeException("Job document not found"));

        try (PDDocument originalDoc = PDDocument.load(docEntity.getOriginalPdf());
             PDDocument outDoc = new PDDocument()) {

            PDFRenderer renderer = new PDFRenderer(originalDoc);

            int totalPages = originalDoc.getNumberOfPages();
            for (int i = 0; i < totalPages; i++) {
                PDPage srcPage = originalDoc.getPage(i);

                // Fetch applied angle from DB
                double angle = pageRepository.findByJobIdAndPageIndex(jobId, i)
                        .map(PageEntity::getAppliedAngle)
                        .orElse(0.0);

                // Render page to image
                BufferedImage pageImage = renderer.renderImageWithDPI(i, 300);

                // Rotate physically
                BufferedImage rotated = PdfDeskewService.rotateImage(pageImage, angle);

                // Add new page
                PDPage newPage = new PDPage(srcPage.getMediaBox());
                outDoc.addPage(newPage);

                var pdImage = JPEGFactory.createFromImage(outDoc, rotated);
                try (PDPageContentStream cs = new PDPageContentStream(outDoc, newPage)) {
                    cs.drawImage(pdImage, 0, 0, newPage.getMediaBox().getWidth(), newPage.getMediaBox().getHeight());
                }
            }

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            outDoc.save(baos);
            return baos.toByteArray();

        } catch (IOException e) {
            throw new RuntimeException("Error reapplying transforms", e);
        }
    }

    private Mat preprocessForSkew(Mat bin) {
        int targetWidth = 1000; // adaptive downsample
        if (bin.cols() > targetWidth) {
            double scale = targetWidth / (double) bin.cols();
            Mat resized = new Mat();
            opencv_imgproc.resize(bin, resized, new Size(0, 0), scale, scale, opencv_imgproc.INTER_AREA);
            return resized;
        }
        return bin;
    }

    private double detectSkewAngle(BufferedImage bi) {
        Mat src = ImageUtils.bufferedImageToMat(bi);
        Mat gray = new Mat();
        opencv_imgproc.cvtColor(src, gray, opencv_imgproc.COLOR_BGR2GRAY);

        Mat bin = new Mat();
        opencv_imgproc.threshold(gray, bin, 0, 255,
                opencv_imgproc.THRESH_BINARY_INV + opencv_imgproc.THRESH_OTSU);

        // Downsample
        Mat small = preprocessForSkew(bin);

        // Coarse scan
        double bestAngle = 0, bestScore = Double.NEGATIVE_INFINITY;
        for (double a = -5; a <= 5; a += 2) {
            double score = projectionProfileScore(small, a);
            if (score > bestScore) {
                bestScore = score;
                bestAngle = a;
            }
        }

        // Gradient descent
        return gradientDescentAngle(small, bestAngle, 2.0, 0.05);
    }

    private double gradientDescentAngle(Mat bin, double initAngle, double initStep, double minStep) {
        double angle = initAngle;
        double step = initStep;
        double bestScore = projectionProfileScore(bin, angle);

        while (step > minStep) {
            // Check neighbors
            double scorePlus = projectionProfileScore(bin, angle + step);
            double scoreMinus = projectionProfileScore(bin, angle - step);


            if (scorePlus > bestScore * 1.01) { // significant improvement
                angle += step;
                bestScore = scorePlus;
                step = Math.min(step * 1.2, 5.0); // speed up
            } else if (scoreMinus > bestScore * 1.01) {
                angle -= step;
                bestScore = scoreMinus;
                step = Math.min(step * 1.2, 5.0);
            } else {
                step /= 2.0; // slow down
            }

        }

        double normalized = normalizeTheta(angle);
        System.out.printf("Gradient descent detected skew: %.3f° (final step=%.3f)%n", normalized, step);
        return normalized;
    }


    private double projectionProfileScore(Mat bin, double angle) {
        // Rotate image by angle
        Mat rotated = new Mat();
        Point2f center = new Point2f(bin.cols() / 2.0f, bin.rows() / 2.0f);
        Mat rot = opencv_imgproc.getRotationMatrix2D(center, angle, 1.0);
        opencv_imgproc.warpAffine(bin, rotated, rot, bin.size(),
                opencv_imgproc.INTER_LINEAR, opencv_core.BORDER_CONSTANT,
                new Scalar(0, 0, 0, 0));

        // Compute horizontal projection profile
        int rows = rotated.rows();
        int cols = rotated.cols();
        int[] hist = new int[rows];
        UByteRawIndexer idx = rotated.createIndexer();

        for (int y = 0; y < rows; y++) {
            int sum = 0;
            for (int x = 0; x < cols; x++) {
                sum += idx.get(y, x) & 0xFF;
            }
            hist[y] = sum;
        }

        // Compute variance of projection (sharper = better alignment)
        double mean = Arrays.stream(hist).average().orElse(0);
        double var = 0;
        for (int v : hist) {
            var += (v - mean) * (v - mean);
        }
        idx.release();
        return var / rows;
    }

    private static double normalizeTheta(double theta) {
        while (theta <= -90) theta += 180;
        while (theta > 90) theta -= 180;
        if (theta > 45) theta -= 90;
        if (theta < -45) theta += 90;
        return theta;
    }



    public static BufferedImage rotateImage(BufferedImage bi, double degrees) {
        Mat src = ImageUtils.bufferedImageToMat(bi);
        Point2f center = new Point2f(src.cols() / 2.0f, src.rows() / 2.0f);
        Mat rot = opencv_imgproc.getRotationMatrix2D(center, degrees, 1.0);
        Size size = new Size(src.cols(), src.rows());
        Mat dst = new Mat(size, src.type());

        opencv_imgproc.warpAffine(src, dst, rot, size,
                opencv_imgproc.INTER_LINEAR, opencv_core.BORDER_CONSTANT,
                new Scalar(255, 255, 255, 255));

        Mat rgb = new Mat();
        opencv_imgproc.cvtColor(dst, rgb, opencv_imgproc.COLOR_BGR2RGB);

        byte[] data = new byte[(int) (rgb.total() * rgb.channels())];
        rgb.data().get(data);

        BufferedImage out = new BufferedImage(rgb.cols(), rgb.rows(), BufferedImage.TYPE_3BYTE_BGR);
        out.getRaster().setDataElements(0, 0, rgb.cols(), rgb.rows(), data);
        return out;
    }

}
