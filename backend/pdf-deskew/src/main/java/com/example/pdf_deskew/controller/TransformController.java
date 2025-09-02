package com.example.pdf_deskew.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.example.pdf_deskew.dto.PageTransformRequest;
import com.example.pdf_deskew.service.TransformService;

import java.util.List;
@CrossOrigin(origins = "http://localhost:4200")
@RestController
@RequestMapping("/api/transform")
public class TransformController {

    private final TransformService transformService;

    public TransformController(TransformService transformService) {
        this.transformService = transformService;
    }

    @PostMapping("/{jobId}")
    public ResponseEntity<Void> applyTransformations(
            @PathVariable String jobId,
            @RequestBody List<PageTransformRequest> transforms) {
        transformService.applyTransforms(jobId, transforms);
        return ResponseEntity.ok().build();
    }
}
