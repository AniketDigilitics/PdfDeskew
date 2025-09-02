package com.example.pdf_deskew.service;

import com.example.pdf_deskew.dto.ReportDto;
import com.example.pdf_deskew.entity.PageEntity;
import com.example.pdf_deskew.repository.JobRepository;
import com.example.pdf_deskew.repository.PageRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ReportService {

    private final JobRepository jobRepository;
    private final PageRepository pageRepository;

    public ReportService(JobRepository jobRepository, PageRepository pageRepository) {
        this.jobRepository = jobRepository;
        this.pageRepository = pageRepository;
    }

    @Transactional(readOnly = true)
    public ReportDto getReport(String jobId) {
        var job = jobRepository.findById(jobId)
                .orElseThrow(() -> new IllegalArgumentException("Job not found: " + jobId));
        List<PageEntity> pages = pageRepository.findByJob_IdOrderByPageIndexAsc(jobId);

        var rotations = pages.stream()
                .map(p -> new ReportDto.RotationItem(
                        p.getPageIndex(),
                        p.getDetectedAngle(),
                        p.getAppliedAngle()
                ))
                .toList();

        return new ReportDto(job.getId(), pages.size(), rotations);
    }
}
