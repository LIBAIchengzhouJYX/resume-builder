package com.resumebuilder.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "app")
public class AppConfig {
    private JwtConfig jwt = new JwtConfig();
    private PdfConfig pdf = new PdfConfig();
    private String frontendUrl = "http://localhost:5173";

    @Data
    public static class JwtConfig {
        private String secret = "default-secret-change-in-production";
        private long expirationMs = 86400000;
    }

    @Data
    public static class PdfConfig {
        private String wkhtmltopdfPath = "/usr/local/bin/wkhtmltopdf";
    }

    public String getJwtSecret() { return jwt.getSecret(); }
    public long getJwtExpirationMs() { return jwt.getExpirationMs(); }
    public String getWkhtmltopdfPath() { return pdf.getWkhtmltopdfPath(); }
}
