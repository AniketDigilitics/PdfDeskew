package com.example.pdf_deskew.controller;

import com.example.pdf_deskew.dto.CreateJobResponse;
import com.example.pdf_deskew.dto.JobSummaryDto;
import com.example.pdf_deskew.dto.PageAngleDto;
import com.example.pdf_deskew.entity.PageEntity;
import com.example.pdf_deskew.repository.PageRepository;
import com.example.pdf_deskew.service.JobService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@CrossOrigin(origins = {"http://localhost:4200", "*"})
@RestController
@RequestMapping("/api")
public class JobController {

    private final JobService jobService;
    private final PageRepository pageRepository;

    public JobController(JobService jobService, PageRepository pageRepository) {
        this.jobService = jobService;
        this.pageRepository = pageRepository;
    }

    // Create job, run detection, persist everything
    @PostMapping(value = "/jobs", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public CreateJobResponse createJob(@RequestPart("file") MultipartFile file) {
        return jobService.createAndProcess(file);
    }

    // Return detected/applied angles per page
    @GetMapping("/pages/{jobId}")
    public List<PageAngleDto> getPages(@PathVariable String jobId) {
        List<PageEntity> pages = pageRepository.findByJob_IdOrderByPageIndexAsc(jobId);
        return pages.stream()
                .map(p -> new PageAngleDto(p.getPageIndex(), p.getDetectedAngle(), p.getAppliedAngle()))
                .toList();
    }
    // Fetch all jobs with metadata
    @GetMapping("/jobs")
    public List<JobSummaryDto> getJobs() {
        return jobService.getAllJobs();
    }

    @DeleteMapping("jobs/{jobId}")
    public ResponseEntity<Void> deleteJob(@PathVariable String jobId) {
        boolean deleted = jobService.deleteJob(jobId);
        if (deleted) {
            return ResponseEntity.noContent().build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }

}
