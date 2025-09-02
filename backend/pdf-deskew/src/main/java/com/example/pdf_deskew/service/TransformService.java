package com.example.pdf_deskew.service;

import com.example.pdf_deskew.dto.PageTransformRequest;
import com.example.pdf_deskew.entity.JobDocumentEntity;
import com.example.pdf_deskew.entity.JobEntity;
import com.example.pdf_deskew.entity.PageEntity;
import com.example.pdf_deskew.repository.JobDocumentRepository;
import com.example.pdf_deskew.repository.JobRepository;
import com.example.pdf_deskew.repository.PageRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;
import java.time.Instant;
import java.util.List;


@Service
public class TransformService {

    private final JobRepository jobRepository;
    private final PageRepository pageRepository;
    private final JobDocumentRepository docRepository;
    private final PdfDeskewService pdfDeskewService;

    public TransformService(JobRepository jobRepository,
                            PageRepository pageRepository,
                            JobDocumentRepository docRepository,
                            PdfDeskewService pdfDeskewService) {
        this.jobRepository = jobRepository;
        this.pageRepository = pageRepository;
        this.docRepository = docRepository;
        this.pdfDeskewService = pdfDeskewService;
    }

    @Transactional
    public void applyTransforms(String jobId, List<PageTransformRequest> transforms) {
        JobEntity job = jobRepository.findById(jobId)
                .orElseThrow(() -> new RuntimeException("Job not found: " + jobId));

        // Update applied angles
        for (PageTransformRequest tr : transforms) {
            PageEntity page = pageRepository.findByJobIdAndPageIndex(jobId, tr.getPageIndex())
                    .orElseThrow(() -> new RuntimeException("Page not found: " + tr.getPageIndex()));
            page.setAppliedAngle(tr.getAppliedAngle());
            pageRepository.save(page);
        }

        // Regenerate corrected PDF
        byte[] correctedPdf = pdfDeskewService.reapplyTransforms(jobId);

        // Save corrected PDF in job_documents
        JobDocumentEntity doc = docRepository.findById(jobId)
                .orElseThrow(() -> new RuntimeException("Job document not found for job: " + jobId));
        doc.setCorrectedPdf(correctedPdf);
        docRepository.saveAndFlush(doc);  // force write to DB

        // Update job status timestamp
        job.setUpdatedAt(Instant.now());
        jobRepository.save(job);
    }
}
