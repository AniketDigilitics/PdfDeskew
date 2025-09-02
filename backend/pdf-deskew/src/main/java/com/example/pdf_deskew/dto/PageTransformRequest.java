package com.example.pdf_deskew.dto;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PageTransformRequest {
    private int pageIndex;
    private double appliedAngle;

    public Double getAppliedAngle() {
        return this.appliedAngle;
    }

    public int getPageIndex() {
        return this.pageIndex;
    }
}