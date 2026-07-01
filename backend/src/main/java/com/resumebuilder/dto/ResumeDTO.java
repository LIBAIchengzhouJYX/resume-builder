package com.resumebuilder.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ResumeDTO {
    private Long id;
    private String title;
    private String content;
    private String customCss;
    private String theme;
    private String language;
    private Boolean isPublic;
    private String description;
    private String createdAt;
    private String updatedAt;
}
