package com.example.pdf_deskew.repository;

import com.example.pdf_deskew.entity.JobDocumentEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JobDocumentRepository extends JpaRepository<JobDocumentEntity, String> {
    void deleteByJobId(String jobId);
}


