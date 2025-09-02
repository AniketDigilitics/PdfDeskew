package com.example.pdf_deskew.repository;

import com.example.pdf_deskew.entity.PageEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface PageRepository extends JpaRepository<PageEntity, Long> {
    // find all pages for a job
    List<PageEntity> findByJobId(String jobId);
    // find a single page by job + pageIndex
    Optional<PageEntity> findByJobIdAndPageIndex(String jobId, int pageIndex);
    List<PageEntity> findByJob_IdOrderByPageIndexAsc(String jobId);
    void deleteAllByJobId(String jobId);
}
