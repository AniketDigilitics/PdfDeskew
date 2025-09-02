package com.example.pdf_deskew.dto;

import java.util.List;

public class ReportDto {
    public static class RotationItem {
        private int pageIndex;
        private double original; // detectedAngle
        private Double applied;  // appliedAngle

        public RotationItem(int pageIndex, double original, Double applied) {
            this.pageIndex = pageIndex;
            this.original = original;
            this.applied = applied;
        }
        public int getPageIndex() { return pageIndex; }
        public double getOriginal() { return original; }
        public Double getApplied() { return applied; }
    }

    private String jobId;
    private int totalPages;
    private List<RotationItem> rotations;

    public ReportDto(String jobId, int totalPages, List<RotationItem> rotations) {
        this.jobId = jobId;
        this.totalPages = totalPages;
        this.rotations = rotations;
    }
    public String getJobId() { return jobId; }
    public int getTotalPages() { return totalPages; }
    public List<RotationItem> getRotations() { return rotations; }
}
