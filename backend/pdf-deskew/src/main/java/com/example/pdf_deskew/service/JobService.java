package com.example.pdf_deskew.service;

import com.example.pdf_deskew.dto.CreateJobResponse;
import com.example.pdf_deskew.dto.JobSummaryDto;
import com.example.pdf_deskew.entity.*;
import com.example.pdf_deskew.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import com.example.pdf_deskew.repository.JobDocumentRepository;
import java.time.Instant;
import java.util.*;

@Service
public class JobService {

    private final JobRepository jobRepository;
    private final PageRepository pageRepository;
    private final JobDocumentRepository docRepository;
    private final PdfDeskewService pdfDeskewService;

    public JobService(JobRepository jobRepository,
                      PageRepository pageRepository,
                      JobDocumentRepository docRepository,
                      PdfDeskewService pdfDeskewService) {
        this.jobRepository = jobRepository;
        this.pageRepository = pageRepository;
        this.docRepository = docRepository;
        this.pdfDeskewService = pdfDeskewService;
    }

    @Transactional
    public CreateJobResponse createAndProcess(MultipartFile file) {
        try {
            String jobId = UUID.randomUUID().toString();

            JobEntity job = new JobEntity();
            job.setId(jobId);
            job.setFilename(file.getOriginalFilename() != null ? file.getOriginalFilename() : "upload.pdf");
            job.setStatus(JobStatus.ANALYZING);
            job.setCreatedAt(Instant.now());
            job.setUpdatedAt(Instant.now());
            jobRepository.save(job);

            JobDocumentEntity doc = new JobDocumentEntity();
            doc.setJobId(jobId);
            doc.setOriginalPdf(file.getBytes());
            docRepository.save(doc);

            // Run detection + correction
            PdfDeskewService.Result result = pdfDeskewService.process(file.getBytes());

            // Persist per-page angles
            List<Double> angles = result.pageAngles();
            for (int i = 0; i < angles.size(); i++) {
                PageEntity p = new PageEntity();
                p.setJob(job);
                p.setPageIndex(i);
                p.setDetectedAngle(angles.get(i));
                p.setAppliedAngle(angles.get(i));
                pageRepository.save(p);
            }

            // Save corrected PDF
            doc.setCorrectedPdf(result.correctedPdf());
            docRepository.save(doc);

            // Mark ready
            job.setStatus(JobStatus.READY);
            job.setUpdatedAt(Instant.now());
            jobRepository.save(job);

            return new CreateJobResponse(jobId, angles.size(), angles);
        } catch (Exception ex) {
            // mark failed if job exists
            throw new RuntimeException("Failed to create/process job: " + ex.getMessage(), ex);
        }
    }
    public List<JobSummaryDto> getAllJobs() {
        List<JobEntity> jobs = jobRepository.findAllByOrderByCreatedAtDesc();

        return jobs.stream().map(job -> {
            int pageCount = pageRepository.findByJob_IdOrderByPageIndexAsc(job.getId()).size();
            return new JobSummaryDto(
                    job.getId(),
                    job.getFilename(),
                    job.getStatus().name(),
                    pageCount,
                    job.getCreatedAt(),
                    job.getUpdatedAt()
            );
        }).toList();
    }

    @Transactional
    public boolean deleteJob(String jobId) {
        if (!jobRepository.existsById(jobId)) {
            return false;
        }
        // Delete pages
        pageRepository.deleteAllByJobId(jobId);
        // Delete job_documents
        docRepository.deleteByJobId(jobId);
        // Delete the job itself
        jobRepository.deleteById(jobId);
        return true;
    }
}