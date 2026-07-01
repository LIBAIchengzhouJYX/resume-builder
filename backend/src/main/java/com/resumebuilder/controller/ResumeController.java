package com.resumebuilder.controller;

import com.resumebuilder.dto.ResumeDTO;
import com.resumebuilder.model.User;
import com.resumebuilder.service.ResumeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/resumes")
@RequiredArgsConstructor
public class ResumeController {

    private final ResumeService resumeService;

    @GetMapping
    public ResponseEntity<List<ResumeDTO>> list(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(resumeService.listByUser(user));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ResumeDTO> get(@PathVariable Long id, @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(resumeService.getById(id, user));
    }

    @PostMapping
    public ResponseEntity<ResumeDTO> create(@Valid @RequestBody ResumeDTO dto, @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(resumeService.create(dto, user));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ResumeDTO> update(@PathVariable Long id, @Valid @RequestBody ResumeDTO dto,
                                            @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(resumeService.update(id, dto, user));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> delete(@PathVariable Long id, @AuthenticationPrincipal User user) {
        resumeService.delete(id, user);
        return ResponseEntity.ok(Map.of("message", "删除成功"));
    }
}
