package com.example.pdf_deskew.dto;

import java.util.List;

public class CreateJobResponse {
    private String jobId;
    private int pageCount;
    private List<Double> detectedAngles;

    public CreateJobResponse(String jobId, int pageCount, List<Double> detectedAngles) {
        this.jobId = jobId;
        this.pageCount = pageCount;
        this.detectedAngles = detectedAngles;
    }
    public String getJobId() { return jobId; }
    public int getPageCount() { return pageCount; }
    public List<Double> getDetectedAngles() { return detectedAngles; }
}
