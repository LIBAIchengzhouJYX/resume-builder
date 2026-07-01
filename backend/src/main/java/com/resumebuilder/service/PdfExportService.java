package com.resumebuilder.service;

import com.resumebuilder.config.AppConfig;
import com.resumebuilder.model.Resume;
import com.resumebuilder.repository.ResumeRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
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
    private final ObjectMapper objectMapper = new ObjectMapper();

    public Resource exportPdf(Long resumeId) throws Exception {
        Resume resume = resumeRepository.findById(resumeId)
                .orElseThrow(() -> new RuntimeException("简历不存在"));

        String html = buildResumeHtml(resume);
        byte[] pdfBytes = renderWithWkhtmltopdf(html);

        return new ByteArrayResource(pdfBytes);
    }

    private String buildResumeHtml(Resume resume) {
        String lang = resume.getLanguage() != null ? resume.getLanguage() : "bilingual";
        String customCss = resume.getCustomCss() != null ? resume.getCustomCss() : "";
        String content = resume.getContent();

        // content is JSON — render it into actual resume HTML
        String renderedContent = renderContentToHtml(content);

        return buildFullPage(renderedContent, customCss, lang);
    }

    private String buildFullPage(String bodyHtml, String customCss, String lang) {
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
                .r-contact { font-size: 9pt; color: #555; display: flex; flex-wrap: wrap; justify-content: center; gap: 4px 14px; }
                .r-contact span { display: inline-block; }
                .r-section { margin-bottom: 12pt; page-break-inside: avoid; }
                .r-section-title {
                    font-size: 11pt; font-weight: 700; letter-spacing: 0.06em;
                    text-transform: uppercase; border-bottom: 1.5px solid #333;
                    padding-bottom: 3pt; margin-bottom: 7pt;
                }
                .r-item { margin-bottom: 7pt; }
                .r-item-header { display: flex; justify-content: space-between; align-items: baseline; flex-wrap: wrap; }
                .r-item-title { font-weight: 600; font-size: 10.5pt; }
                .r-item-sub { font-size: 9pt; color: #555; }
                .r-item-date { font-size: 8.5pt; color: #777; white-space: nowrap; }
                .r-item-desc { font-size: 9.5pt; margin-top: 2pt; color: #444; }
                .r-item-desc ul { padding-left: 14pt; margin: 2pt 0; }
                .r-item-desc li { margin-bottom: 1pt; }
                .r-item-desc p { margin: 2pt 0; }
                .r-item-desc strong { font-weight: 600; }
                .r-summary { font-size: 10pt; color: #444; line-height: 1.7; }
                .r-summary p { margin: 3pt 0; }
                .r-bilingual-divider { border: none; border-top: 1px dashed #ccc; margin: 8pt 0; }
                .r-skill-line { font-size: 9.5pt; margin-bottom: 2pt; }
                h3 { font-size: 10.5pt; font-weight: 600; margin: 6pt 0 2pt; }
                h4 { font-size: 10pt; font-weight: 600; margin: 4pt 0 1pt; }
                blockquote { border-left: 2px solid #ddd; padding-left: 8pt; color: #666; margin: 4pt 0; }
                code { background: #f0f0f0; padding: 1px 3px; border-radius: 2px; font-size: 9pt; }
                hr { border: none; border-top: 1px solid #e5e5e5; margin: 6pt 0; }
                %s
            </style></head>
            <body>%s</body>
            </html>
            """.formatted(lang, customCss, bodyHtml);
    }

    /**
     * Direct HTML-to-PDF — frontend sends rendered HTML
     */
    public byte[] renderFromHtml(String html) throws Exception {
        String fullPage = buildFullPage(html, "", "bilingual");
        return renderWithWkhtmltopdf(fullPage);
    }

    /**
     * Render JSON resume content into HTML (mirrors frontend EditorPage computedPreviewHtml)
     */
    p