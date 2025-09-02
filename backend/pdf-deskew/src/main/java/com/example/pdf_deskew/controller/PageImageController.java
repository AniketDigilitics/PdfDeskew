package com.example.pdf_deskew.controller;

import com.example.pdf_deskew.entity.PageEntity;
import com.example.pdf_deskew.dto.PageAngleDto;
import com.example.pdf_deskew.entity.JobDocumentEntity;
import com.example.pdf_deskew.repository.JobDocumentRepository;
import com.example.pdf_deskew.repository.PageRepository;
import com.example.pdf_deskew.service.PdfDeskewService;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.ImageType;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;


@CrossOrigin(origins = {"http://localhost:4200", "*"})
@RestController
@RequestMapping("/api/pages")
public class PageImageController {

    private final JobDocumentRepository docRepository;
    private final PdfDeskewService pdfDeskewService;
    private final PageRepository pageRepository;
    private final JobDocumentRepository jobDocumentRepository;

    public PageImageController(JobDocumentRepository docRepository, PdfDeskewService pdfDeskewService,
                               PageRepository pageRepository, JobDocumentRepository jobDocumentRepository) {
        this.docRepository = docRepository;
        this.pdfDeskewService = pdfDeskewService;
        this.pageRepository = pageRepository;
        this.jobDocumentRepository = jobDocumentRepository;
    }

    // getPage function
    @GetMapping("/{jobId}/page/{pageIndex}")
    public ResponseEntity<byte[]> getPage(
            @PathVariable String jobId,
            @PathVariable int pageIndex,
            @RequestParam(defaultValue = "false") boolean original) throws IOException {

        JobDocumentEntity doc = jobDocumentRepository.findById(jobId)
                .orElseThrow(() -> new RuntimeException("Document not found"));

        // Load original PDF always
        byte[] pdfBytes = doc.getOriginalPdf();
        BufferedImage img;

        try (PDDocument pdf = PDDocument.load(pdfBytes)) {
            PDFRenderer renderer = new PDFRenderer(pdf);
            img = renderer.renderImageWithDPI(pageIndex, 100, ImageType.RGB);
        }

        if (!original) {
            // Apply saved rotation for corrected view dynamically
            PageEntity page = pageRepository.findByJobIdAndPageIndex(jobId, pageIndex)
                    .orElseThrow(() -> new RuntimeException("Page not found"));
            double angle = page.getAppliedAngle();
            if (angle != 0) {
                img = PdfDeskewService.rotateImage(img, angle);
            }
        }

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(img, "png", baos);

        return ResponseEntity.ok()
                .contentType(MediaType.IMAGE_PNG)
                .body(baos.toByteArray());
    }


    @GetMapping("/{jobId}/original/{pageIndex}")
    public ResponseEntity<byte[]> getOriginalPage(
            @PathVariable String jobId,
            @PathVariable int pageIndex) throws IOException {

        JobDocumentEntity doc = docRepository.findById(jobId)
                .orElseThrow(() -> new RuntimeException("Document not found"));

        try (PDDocument pdf = PDDocument.load(doc.getOriginalPdf())) {
            PDFRenderer renderer = new PDFRenderer(pdf);
            BufferedImage img = renderer.renderImageWithDPI(pageIndex, 100, ImageType.RGB);

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(img, "png", baos);

            return ResponseEntity.ok()
                    .contentType(MediaType.IMAGE_PNG)
                    .body(baos.toByteArray());
        }
    }

    @GetMapping("/{jobId}/corrected/{pageIndex}")
    public ResponseEntity<byte[]> getCorrectedPage(
            @PathVariable String jobId,
            @PathVariable int pageIndex) throws IOException {

        JobDocumentEntity doc = docRepository.findById(jobId)
                .orElseThrow(() -> new RuntimeException("Document not found"));

        try (PDDocument pdf = PDDocument.load(doc.getCorrectedPdf())) {
            PDFRenderer renderer = new PDFRenderer(pdf);
            BufferedImage img = renderer.renderImageWithDPI(pageIndex, 100, ImageType.RGB);

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(img, "png", baos);

            return ResponseEntity.ok()
                    .contentType(MediaType.IMAGE_PNG)
                    .body(baos.toByteArray());
        }
    }

    @GetMapping("/{jobId}/rotated/{pageIndex}")
    public ResponseEntity<byte[]> getRotatedPage(
            @PathVariable String jobId,
            @PathVariable int pageIndex,
            @RequestParam double angle) throws IOException {

        JobDocumentEntity doc = docRepository.findById(jobId)
                .orElseThrow(() -> new RuntimeException("Document not found"));

        try (PDDocument pdf = PDDocument.load(doc.getOriginalPdf())) {
            PDFRenderer renderer = new PDFRenderer(pdf);
            BufferedImage img = renderer.renderImageWithDPI(pageIndex, 100, ImageType.RGB);

            BufferedImage rotated = PdfDeskewService.rotateImage(img, angle);

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(rotated, "png", baos);

            return ResponseEntity.ok()
                    .contentType(MediaType.IMAGE_PNG)
                    .body(baos.toByteArray());
        }
    }

    @PostMapping("/{jobId}/save")
    public ResponseEntity<String> saveEdits(
            @PathVariable String jobId,
            @RequestBody List<PageAngleDto> edits) throws IOException {

        // Update applied angles in DB
        for (PageAngleDto edit : edits) {
            PageEntity page = pageRepository.findByJobIdAndPageIndex(jobId, edit.getPageIndex())
                    .orElseThrow(() -> new RuntimeException("Page not found"));
            page.setAppliedAngle(edit.getAppliedAngle());
            pageRepository.save(page);
        }

        // Regenerate corrected PDF using original PDF + applied angles
        JobDocumentEntity docEntity = jobDocumentRepository.findById(jobId)
                .orElseThrow(() -> new RuntimeException("Job document not found"));
        byte[] updatedPdf = pdfDeskewService.reapplyTransforms(jobId);
        docEntity.setCorrectedPdf(updatedPdf);

        // Save immediately
        jobDocumentRepository.saveAndFlush(docEntity);

        return ResponseEntity.ok("Edits saved and PDF updated successfully");
    }

}