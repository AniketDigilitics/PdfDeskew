package com.example.pdf_deskew.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "pages",
        uniqueConstraints = @UniqueConstraint(name = "uq_job_page", columnNames = {"job_id", "page_index"}))
public class PageEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Owning side
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "job_id", nullable = false)
    private JobEntity job;

    @Column(name = "page_index", nullable = false)
    private int pageIndex;

    @Column(name = "detected_angle", nullable = false)
    private double detectedAngle;

    @Column(name = "applied_angle")
    private Double appliedAngle;

    // Getters/Setters
    public Long getId() { return id; }
    public JobEntity getJob() { return job; }
    public void setJob(JobEntity job) { this.job = job; }
    public int getPageIndex() { return pageIndex; }
    public void setPageIndex(int pageIndex) { this.pageIndex = pageIndex; }
    public double getDetectedAngle() { return detectedAngle; }
    public void setDetectedAngle(double detectedAngle) { this.detectedAngle = detectedAngle; }
    public Double getAppliedAngle() { return appliedAngle; }
    public void setAppliedAngle(Double appliedAngle) { this.appliedAngle = appliedAngle; }
}
