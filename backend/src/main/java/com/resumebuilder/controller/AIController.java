package com.resumebuilder.controller;

import com.resumebuilder.dto.AiRequest;
import com.resumebuilder.service.AIService;
import com.resumebuilder.service.AIService.AiConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/ai")
@RequiredArgsConstructor
public class AIController {

    private final AIService aiService;

    /**
     * 统一 AI 入口
     * {
     *   "action": "IMPROVE" | "GENERATE" | "ANALYZE" | "TRANSLATE",
     *   "text": "...",           // IMPROVE / TRANSLATE 用
     *   "keywords": "...",       // GENERATE 用
     *   "jobTitle": "...",       // GENERATE 用
     *   "background": "...",     // GENERATE 用
     *   "resumeContent": "...",  // ANALYZE 用
     *   "translateDirection": "zh2en" | "en2zh" // TRANSLATE 用
     * }
     *
     * Headers:
     *   X-AI-Endpoint: https://api.deepseek.com  (或 GLM 地址)
     *   X-AI-ApiKey: sk-xxxxxx
     *   X-AI-Model: deepseek-chat  (或 glm-4 等)
     */
    @PostMapping
    public ResponseEntity<Map<String, String>> aiAction(
            @RequestBody AiRequest request,
            @RequestHeader("X-AI-Endpoint") String endpoint,
            @RequestHeader("X-AI-ApiKey") String apiKey,
            @RequestHeader("X-AI-Model") String model) {

        AiConfig config = new AiConfig(endpoint, apiKey, model);

        try {
            String result = switch (request.getAction()) {
                case IMPROVE -> aiService.improve(request.getText(), config);
                case GENERATE -> aiService.generate(
                        request.getKeywords(), request.getJobTitle(), request.getBackground(), config);
                case ANALYZE -> aiService.analyze(request.getResumeContent(), config);
                case TRANSLATE -> aiService.translate(request.getText(), request.getTranslateDirection(), config);
            };
            return ResponseEntity.ok(Map.of("result", result));
        } catch (Exception e) {
            log.error("AI action failed: action={}, error={}", request.getAction(), e.getMessage());
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage() != null ? e.getMessage() : "AI 请求失败"));
        }
    }

    /**
     * 获取预设的 AI 模型列表（方便用户选择）
     */
    @GetMapping("/models")
    public ResponseEntity<Map<String, Object>> listModels() {
        var models = java.util.List.of(
                Map.of(
                        "name", "DeepSeek V3",
                        "model", "deepseek-chat",
                        "endpoint", "https://api.deepseek.com",
                        "description", "DeepSeek 最新通用模型，性价比极高"
                ),
                Map.of(
                        "name", "DeepSeek R1",
                        "model", "deepseek-reasoner",
                        "endpoint", "https://api.deepseek.com",
                        "description", "DeepSeek 推理模型，适合复杂的分析和建议任务"
                ),
                Map.of(
                        "name", "GLM-4",
                        "model", "glm-4",
                        "endpoint", "https://open.bigmodel.cn/api/paas/v4",
                        "description", "智谱 GLM-4 旗舰模型，中文能力强"
                ),
                Map.of(
                        "name", "GLM-4 Flash",
                        "model", "glm-4-flash",
                        "endpoint", "https://open.bigmodel.cn/api/paas/v4",
                        "description", "GLM-4 轻量版，速度更快"
                ),
                Map.of(
                        "name", "OpenAI GPT-4o",
                        "model", "gpt-4o",
                        "endpoint", "https://api.openai.com",
                        "description", "OpenAI 最强模型，支持多语言"
                ),
                Map.of(
                        "name", "OpenAI GPT-4o Mini",
                        "model", "gpt-4o-mini",
                        "endpoint", "https://api.openai.com",
                        "description", "OpenAI 轻量模型，速度快成本低"
                ),
                Map.of(
                        "name", "自定义 / Custom",
                        "model", "",
                        "endpoint", "",
                        "description", "输入任意兼容 OpenAI 协议的 API 地址和模型名"
                )
        );

        return ResponseEntity.ok(Map.of(
                "models", models,
                "protocol", "OpenAI-compatible (POST /v1/chat/completions)",
                "note", "所有模型 API Key 由用户自己提供，通过请求头 X-AI-ApiKey 传入"
        ));
    }
}
