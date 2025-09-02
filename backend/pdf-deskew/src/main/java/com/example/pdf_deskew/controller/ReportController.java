package com.example.pdf_deskew.controller;

import com.example.pdf_deskew.dto.ReportDto;
import com.example.pdf_deskew.service.ReportService;
import org.springframework.web.bind.annotation.*;

@CrossOrigin(origins = {"http://localhost:4200", "*"})
@RestController
@RequestMapping("/api/report")
public class ReportController {

    private final ReportService reportService;
    public ReportController(ReportService reportService) {
        this.reportService = reportService;
    }

    @GetMapping("/{jobId}")
    public ReportDto getReport(@PathVariable String jobId) {
        return reportService.getReport(jobId);
    }
}
