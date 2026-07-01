package com.resumebuilder.service;

import com.resumebuilder.config.AppConfig;
import com.resumebuilder.model.Resume;
import com.resumebuilder.repository.ResumeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class PdfExportService {

    private final AppConfig appConfig;
    private final ResumeRepository resumeRepository;

    public Resource exportPdf(Long resumeId) throws Exception {
        Resume resume = resumeRepository.findById(resumeId)
                .orElseThrow(() -> new RuntimeException("简历不存在"));

        String html = buildResumeHtml(resume);
        byte[] pdfBytes = renderWithWkhtmltopdf(html);

        return new ByteArrayResource(pdfBytes);
    }

    private String buildResumeHtml(Resume resume) {
        // Build a complete HTML page for the resume
        String theme = resume.getTheme() != null ? resume.getTheme() : "modern";
        String lang = resume.getLanguage() != null ? resume.getLanguage() : "bilingual";
        String customCss = resume.getCustomCss() != null ? resume.getCustomCss() : "";
        String content = resume.getContent();

        return """
            <!DOCTYPE html>
            <html lang="%s">
            <head><meta charset="UTF-8">
            <style>
                @page { size: A4; margin: 15mm 18mm; }
                body {
                    font-family: 'Georgia', 'Noto Serif SC', 'Songti SC', serif;
                    font-size: 10.5pt; line-height: 1.6; color: #222;
                }
                .r-header { text-align: center; margin-bottom: 14pt; }
                .r-name { font-size: 22pt; font-weight: 700; letter-spacing: 0.04em; margin-bottom: 4pt; }
                .r-contact { font-size: 9pt; color: #555; }
                .r-section { margin-bottom: 12pt; }
                .r-section-title {
                    font-size: 11pt; font-weight: 700; letter-spacing: 0.06em;
                    text-transform: uppercase; border-bottom: 1.5px solid #333;
                    padding-bottom: 3pt; margin-bottom: 7pt;
                }
                .r-item { margin-bottom: 7pt; }
                .r-item-header { display: flex; justify-content: space-between; }
                .r-item-title { font-weight: 600; }
                .r-item-sub { font-size: 9pt; color: #555; }
                .r-item-date { font-size: 8.5pt; color: #777; }
                .r-item-desc { font-size: 9.5pt; margin-top: 2pt; color: #444; }
                .r-item-desc ul { padding-left: 14pt; margin: 2pt 0; }
                .r-item-desc li { margin-bottom: 1pt; }
                .r-summary { font-size: 10pt; color: #444; line-height: 1.7; }
                .r-bilingual-divider { border: none; border-top: 1px dashed #ccc; margin: 8pt 0; }
                %s
            </style></head>
            <body>%s</body>
            </html>
            """.formatted(lang, customCss, content);
    }

    private byte[] renderWithWkhtmltopdf(String html) throws Exception {
        Path tempHtml = null;
        Path tempPdf = null;
        try {
            tempHtml = Files.createTempFile("resume-", ".html");
            tempPdf = Files.createTempFile("resume-", ".pdf");
            Files.writeString(tempHtml, html, StandardCharsets.UTF_8);

            String wkPath = appConfig.getWkhtmltopdfPath();
            ProcessBuilder pb = new ProcessBuilder(
                wkPath,
                "--page-size", "A4",
                "--margin-top", "0",
                "--margin-bottom", "0",
                "--margin-left", "0",
                "--margin-right", "0",
                "--encoding", "UTF-8",
                "--no-outline",
                tempHtml.toString(),
                tempPdf.toString()
            );

            Process process = pb.start();
            boolean finished = process.waitFor(30, TimeUnit.SECONDS);
            if (!finished) {
                process.destroyForcibly();
                throw new RuntimeException("PDF generation timed out");
            }

            int exitCode = process.exitValue();
            if (exitCode != 0) {
                String stderr = new String(process.getErrorStream().readAllBytes(), StandardCharsets.UTF_8);
                throw new RuntimeException("wkhtmltopdf failed: " + stderr);
            }

            return Files.readAllBytes(tempPdf);
        } finally {
            if (tempHtml != null) Files.deleteIfExists(tempHtml);
            if (tempPdf != null) Files.deleteIfExists(tempPdf);
        }
    }
}
