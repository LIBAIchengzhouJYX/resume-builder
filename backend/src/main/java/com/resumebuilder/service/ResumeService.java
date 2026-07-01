package com.resumebuilder.service;

import com.resumebuilder.dto.ResumeDTO;
import com.resumebuilder.model.Resume;
import com.resumebuilder.model.User;
import com.resumebuilder.repository.ResumeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ResumeService {

    private final ResumeRepository resumeRepository;
    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    public List<ResumeDTO> listByUser(User user) {
        return resumeRepository.findByUserOrderByUpdatedAtDesc(user)
                .stream().map(this::toDTO).toList();
    }

    public ResumeDTO getById(Long id, User user) {
        Resume r = resumeRepository.findByIdAndUser(id, user)
                .orElseThrow(() -> new RuntimeException("简历不存在"));
        return toDTO(r);
    }

    @Transactional
    public ResumeDTO create(ResumeDTO dto, User user) {
        Resume r = Resume.builder()
                .title(dto.getTitle())
                .content(dto.getContent())
                .customCss(dto.getCustomCss())
                .theme(dto.getTheme() != null ? dto.getTheme() : "modern")
                .language(dto.getLanguage() != null ? dto.getLanguage() : "bilingual")
                .isPublic(dto.getIsPublic() != null ? dto.getIsPublic() : false)
                .description(dto.getDescription())
                .user(user)
                .build();
        return toDTO(resumeRepository.save(r));
    }

    @Transactional
    public ResumeDTO update(Long id, ResumeDTO dto, User user) {
        Resume r = resumeRepository.findByIdAndUser(id, user)
                .orElseThrow(() -> new RuntimeException("简历不存在"));
        if (dto.getTitle() != null) r.setTitle(dto.getTitle());
        if (dto.getContent() != null) r.setContent(dto.getContent());
        if (dto.getCustomCss() != null) r.setCustomCss(dto.getCustomCss());
        if (dto.getTheme() != null) r.setTheme(dto.getTheme());
        if (dto.getLanguage() != null) r.setLanguage(dto.getLanguage());
        if (dto.getIsPublic() != null) r.setIsPublic(dto.getIsPublic());
        if (dto.getDescription() != null) r.setDescription(dto.getDescription());
        return toDTO(resumeRepository.save(r));
    }

    @Transactional
    public void delete(Long id, User user) {
        Resume r = resumeRepository.findByIdAndUser(id, user)
                .orElseThrow(() -> new RuntimeException("简历不存在"));
        resumeRepository.delete(r);
    }

    private ResumeDTO toDTO(Resume r) {
        return ResumeDTO.builder()
                .id(r.getId()).title(r.getTitle())
                .content(r.getContent()).customCss(r.getCustomCss())
                .theme(r.getTheme()).language(r.getLanguage())
                .isPublic(r.getIsPublic()).description(r.getDescription())
                .createdAt(r.getCreatedAt().format(FMT))
                .updatedAt(r.getUpdatedAt() != null ? r.getUpdatedAt().format(FMT) : null)
                .build();
    }
}
