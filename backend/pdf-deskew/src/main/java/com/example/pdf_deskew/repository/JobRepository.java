package com.example.pdf_deskew.repository;

import com.example.pdf_deskew.entity.JobEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface JobRepository extends JpaRepository<JobEntity, String> {
    // Fetch all jobs ordered by created date
    List<JobEntity> findAllByOrderByCreatedAtDesc();
}
