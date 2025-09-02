package com.example.pdf_deskew.controller;


import com.example.pdf_deskew.entity.JobDocumentEntity;
import com.example.pdf_deskew.repository.JobDocumentRepository;
import com.example.pdf_deskew.service.PdfRenderService;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@CrossOrigin(origins = {"http://localhost:4200", "*"})
@RestController
@RequestMapping("/api/pdf")
public class PdfController {

    private final PdfRenderService pdfRenderService;
    private final JobDocumentRepository docRepository;

    public PdfController(PdfRenderService pdfRenderService, JobDocumentRepository docRepository) {
        this.pdfRenderService = pdfRenderService;
        this.docRepository = docRepository;
    }

    @GetMapping("/{jobId}/original/{pageIndex}")
    public ResponseEntity<ByteArrayResource> getOriginalPage(
            @PathVariable String jobId,
            @PathVariable int pageIndex) throws Exception {

        JobDocumentEntity doc = docRepository.findById(jobId)
                .orElseThrow(() -> new IllegalArgumentException("Document not found for job: " + jobId));

        byte[] pdfBytes = doc.getOriginalPdf();
        List<byte[]> pages = pdfRenderService.renderPdfToImages(pdfBytes);

        if (pageIndex < 0 || pageIndex >= pages.size()) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok()
                .contentType(MediaType.IMAGE_PNG)
                .body(new ByteArrayResource(pages.get(pageIndex)));
    }

    @GetMapping("/{jobId}/corrected/{pageIndex}")
    public ResponseEntity<ByteArrayResource> getCorrectedPage(
            @PathVariable String jobId,
            @PathVariable int pageIndex) throws Exception {

        JobDocumentEntity doc = docRepository.findById(jobId)
                .orElseThrow(() -> new IllegalArgumentException("Document not found for job: " + jobId));

        byte[] pdfBytes = doc.getCorrectedPdf();
        List<byte[]> pages = pdfRenderService.renderPdfToImages(pdfBytes);

        if (pageIndex < 0 || pageIndex >= pages.size()) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok()
                .contentType(MediaType.IMAGE_PNG)
                .body(new ByteArrayResource(pages.get(pageIndex)));
    }
}
