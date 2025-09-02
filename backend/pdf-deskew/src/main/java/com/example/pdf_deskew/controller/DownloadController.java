package com.example.pdf_deskew.controller;

import com.example.pdf_deskew.entity.JobEntity;
import com.example.pdf_deskew.entity.JobStatus;
import com.example.pdf_deskew.entity.JobDocumentEntity;
import com.example.pdf_deskew.repository.JobDocumentRepository;
import com.example.pdf_deskew.repository.JobRepository;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;


@CrossOrigin(origins = {"http://localhost:4200", "*"})
@RestController
@RequestMapping("/api/download")
public class DownloadController {

    private final JobRepository jobRepository;
    private final JobDocumentRepository docRepository;

    public DownloadController(JobRepository jobRepository, JobDocumentRepository docRepository) {
        this.jobRepository = jobRepository;
        this.docRepository = docRepository;
    }

    /**
     * Download a PDF.
     * @param jobId the job ID
     * @param original if true, download the original uploaded PDF; otherwise, download deskewed PDF
     */
    @GetMapping("/{jobId}")
    public ResponseEntity<byte[]> download(
            @PathVariable String jobId,
            @RequestParam(defaultValue = "false") boolean original) {

        JobEntity job = jobRepository.findById(jobId)
                .orElseThrow(() -> new IllegalArgumentException("Job not found: " + jobId));

        if (!original && job.getStatus() != JobStatus.READY) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .contentType(MediaType.TEXT_PLAIN)
                    .body(("Job not ready. Current status: " + job.getStatus()).getBytes());
        }

        JobDocumentEntity doc = docRepository.findById(jobId)
                .orElseThrow(() -> new IllegalArgumentException("Document not found for job: " + jobId));

        byte[] pdfData = original ? doc.getOriginalPdf() : doc.getCorrectedPdf();
        if (pdfData == null || pdfData.length == 0) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .contentType(MediaType.TEXT_PLAIN)
                    .body(("Requested PDF not found").getBytes());
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);

        String suffix = original ? "_original.pdf" : "_deskewed.pdf";
        String name = job.getFilename().replaceFirst("\\.pdf$", "") + suffix;
        headers.setContentDisposition(ContentDisposition.attachment().filename(name).build());

        return new ResponseEntity<>(pdfData, headers, HttpStatus.OK);
    }
}
