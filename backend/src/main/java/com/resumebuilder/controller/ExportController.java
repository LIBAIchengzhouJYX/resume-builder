package com.resumebuilder.controller;

import com.resumebuilder.model.User;
import com.resumebuilder.service.PdfExportService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/export")
@RequiredArgsConstructor
public class ExportController {

    private final PdfExportService pdfExportService;

    @GetMapping("/pdf/{resumeId}")
    public ResponseEntity<Resource> exportPdf(@PathVariable Long resumeId,
                                               @AuthenticationPrincipal User user) {
        try {
            Resource pdf = pdfExportService.exportPdf(resumeId);
            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_PDF)
                    .header(HttpHeaders.CONTENT_DISPOSITION,
                            "attachment; filename=\"resume.pdf\"")
                    .body(pdf);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
}
