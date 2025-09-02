package com.example.pdf_deskew.dto;

import java.time.Instant;

public record JobSummaryDto(
        String id,
        String filename,
        String status,
        int pageCount,
        Instant createdAt,
        Instant updatedAt
) {}
