package com.resumebuilder.controller;

import com.resumebuilder.dto.UserDTO;
import com.resumebuilder.model.User;
import com.resumebuilder.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserRepository userRepository;

    @GetMapping("/me")
    public ResponseEntity<UserDTO> me(@AuthenticationPrincipal User user) {
        if (user == null) {
            return ResponseEntity.status(401).build();
        }
        return ResponseEntity.ok(UserDTO.builder()
                .id(user.getId()).name(user.getName()).login(user.getLogin())
                .email(user.getEmail()).avatarUrl(user.getAvatarUrl())
                .bio(user.getBio()).role(user.getRole().name()).build());
    }

    @GetMapping("/github-url")
    public ResponseEntity<Map<String, String>> githubLoginUrl() {
        return ResponseEntity.ok(Map.of(
            "url", "/oauth2/authorization/github"
        ));
    }
}
