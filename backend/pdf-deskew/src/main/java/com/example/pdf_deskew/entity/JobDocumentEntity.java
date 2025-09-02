package com.example.pdf_deskew.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "job_documents")
public class JobDocumentEntity {
    @Id
    @Column(name = "job_id", nullable = false, length = 36)
    private String jobId;

    @Lob
    @Column(name = "original_pdf", nullable = false)
    private byte[] originalPdf;

    @Lob
    @Column(name = "corrected_pdf")
    private byte[] correctedPdf;

    // Getters/Setters
    public String getJobId() { return jobId; }
    public void setJobId(String jobId) { this.jobId = jobId; }
    public byte[] getOriginalPdf() { return originalPdf; }
    public void setOriginalPdf(byte[] originalPdf) { this.originalPdf = originalPdf; }
    public byte[] getCorrectedPdf() { return correctedPdf; }
    public void setCorrectedPdf(byte[] correctedPdf) { this.correctedPdf = correctedPdf; }
}
