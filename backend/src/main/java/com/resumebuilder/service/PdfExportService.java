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
        String renderedContent = renderContentToHtml(content);
        return buildFullPage(renderedContent, customCss, lang);
    }

    private String buildFullPage(String bodyHtml, String customCss, String lang) {
        return "<!DOCTYPE html>\n<html lang=\"" + lang + "\">\n<head><meta charset=\"UTF-8\">\n<style>\n" +
            "@page { size: A4; margin: 15mm 18mm; }\n" +
            "body { font-family: 'Georgia', 'Noto Serif SC', 'Songti SC', serif; font-size: 10.5pt; line-height: 1.6; color: #222; }\n" +
            ".r-header { text-align: center; margin-bottom: 14pt; }\n" +
            ".r-name { font-size: 22pt; font-weight: 700; letter-spacing: 0.04em; margin-bottom: 4pt; }\n" +
            ".r-contact { font-size: 9pt; color: #555; display: flex; flex-wrap: wrap; justify-content: center; gap: 4px 14px; }\n" +
            ".r-section { margin-bottom: 12pt; page-break-inside: avoid; }\n" +
            ".r-section-title { font-size: 11pt; font-weight: 700; letter-spacing: 0.06em; text-transform: uppercase; border-bottom: 1.5px solid #333; padding-bottom: 3pt; margin-bottom: 7pt; }\n" +
            ".r-item { margin-bottom: 7pt; }\n" +
            ".r-item-header { display: flex; justify-content: space-between; align-items: baseline; flex-wrap: wrap; }\n" +
            ".r-item-title { font-weight: 600; font-size: 10.5pt; }\n" +
            ".r-item-sub { font-size: 9pt; color: #555; }\n" +
            ".r-item-date { font-size: 8.5pt; color: #777; white-space: nowrap; }\n" +
            ".r-item-desc { font-size: 9.5pt; margin-top: 2pt; color: #444; }\n" +
            ".r-item-desc ul { padding-left: 14pt; margin: 2pt 0; }\n" +
            ".r-item-desc li { margin-bottom: 1pt; }\n" +
            "h3 { font-size: 10.5pt; font-weight: 600; margin: 6pt 0 2pt; }\n" +
            "blockquote { border-left: 2px solid #ddd; padding-left: 8pt; color: #666; margin: 4pt 0; }\n" +
            "code { background: #f0f0f0; padding: 1px 3px; border-radius: 2px; font-size: 9pt; }\n" +
            "hr { border: none; border-top: 1px solid #e5e5e5; margin: 6pt 0; }\n" +
            customCss + "\n</style></head>\n<body>" + bodyHtml + "</body>\n</html>";
    }

    public byte[] renderFromHtml(String html) throws Exception {
        String fullPage = buildFullPage(html, "", "bilingual");
        return renderWithWkhtmltopdf(fullPage);
    }

    private String renderContentToHtml(String content) {
        try {
            JsonNode d = objectMapper.readTree(content);
            StringBuilder h = new StringBuilder();
            String name = safeStr(d, "nameCN"), nameEn = safeStr(d, "nameEN");
            String phone = safeStr(d, "phone"), email = safeStr(d, "email");
            String github = safeStr(d, "github"), website = safeStr(d, "website"), location = safeStr(d, "location");
            h.append("<div class=\"r-header\"><div class=\"r-name\">").append(esc(name));
            if (!nameEn.isEmpty()) h.append(" / ").append(esc(nameEn));
            h.append("</div><div class=\"r-contact\">");
            if (!phone.isEmpty()) h.append("<span>").append(esc(phone)).append("</span>");
            if (!email.isEmpty()) h.append("<span>").append(esc(email)).append("</span>");
            if (!github.isEmpty()) h.append("<span>").append(esc(github)).append("</span>");
            if (!website.isEmpty()) h.app