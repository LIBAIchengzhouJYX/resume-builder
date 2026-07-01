package com.resumebuilder.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "resumes")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Resume {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;  // e.g. "张三-后端开发-2026"

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    /**
     * 完整简历数据，JSON 格式存储
     * 结构：{ personalInfo, summary, skills, workExperience[], projects[], education[], certifications[], customSections[] }
     */
    @Column(columnDefinition = "LONGTEXT")
    private String content;

    /**
     * 自定义 CSS 变量和样式覆盖
     */
    @Column(columnDefinition = "TEXT")
    private String customCss;

    /**
     * 简历主题名称：modern, minimal, sidebar, timeline, classic, bold
     */
    @Column(nullable = false)
    @Builder.Default
    private String theme = "modern";

    /**
     * 语言：zh, en, bilingual
     */
    @Column(nullable = false)
    @Builder.Default
    private String language = "bilingual";

    @Builder.Default
    private Boolean isPublic = false;

    @Column(length = 500)
    private String description;

    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    private LocalDateTime updatedAt;

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
