package com.example.pdf_deskew.dto;

public class PageAngleDto {
    private int pageIndex;
    private double detectedAngle;
    private Double appliedAngle;

    public PageAngleDto(int pageIndex, double detectedAngle, Double appliedAngle) {
        this.pageIndex = pageIndex;
        this.detectedAngle = detectedAngle;
        this.appliedAngle = appliedAngle;
    }
    public int getPageIndex() { return pageIndex; }
    public double getDetectedAngle() { return detectedAngle; }
    public Double getAppliedAngle() { return appliedAngle; }
}
