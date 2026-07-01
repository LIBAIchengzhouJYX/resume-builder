package com.resumebuilder.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.resumebuilder.dto.AiRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.List;

@Slf4j
@Service
public class AIService {

    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;

    public AIService() {
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(15))
                .build();
        this.objectMapper = new ObjectMapper();
    }

    public String improve(String text, AiConfig config) throws Exception {
        String prompt = buildImprovePrompt(text);
        return callAI(prompt, config);
    }

    public String generate(String keywords, String jobTitle, String background, AiConfig config) throws Exception {
        String prompt = buildGeneratePrompt(keywords, jobTitle, background);
        return callAI(prompt, config);
    }

    public String analyze(String resumeContent, AiConfig config) throws Exception {
        String prompt = buildAnalyzePrompt(resumeContent);
        return callAI(prompt, config);
    }

    public String translate(String text, String direction, AiConfig config) throws Exception {
        String prompt = buildTranslatePrompt(text, direction);
        return callAI(prompt, config);
    }

    /**
     * 调用 OpenAI 兼容 API（DeepSeek、GLM 都支持此协议）
     * POST {endpoint}/v1/chat/completions
     */
    private String callAI(String userPrompt, AiConfig config) throws Exception {
        String endpoint = config.getEndpoint();
        // 去掉末尾的 /
        if (endpoint.endsWith("/")) endpoint = endpoint.substring(0, endpoint.length() - 1);
        String url = endpoint + "/v1/chat/completions";

        var systemMsg = objectMapper.createObjectNode()
                .put("role", "system")
                .put("content", "你是一个专业的简历顾问和职业写作专家，精通中文和英文简历撰写。回答问题时使用与用户提问相同的语言。输出内容保持专业、简洁、有说服力。");

        var userMsg = objectMapper.createObjectNode()
                .put("role", "user")
                .put("content", userPrompt);

        var messages = objectMapper.createArrayNode().add(systemMsg).add(userMsg);

        var body = objectMapper.createObjectNode()
                .put("model", config.getModel())
                .put("temperature", 0.7)
                .put("max_tokens", 4096);
        body.set("messages", messages);

        String jsonBody = objectMapper.writeValueAsString(body);

        var request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + config.getApiKey())
                .timeout(Duration.ofSeconds(120))
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                .build();

        log.info("AI Request: model={}, endpoint={}", config.getModel(), endpoint);

        var response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            String errBody = response.body();
            log.error("AI API error: status={}, body={}", response.statusCode(), errBody);
            // 尝试提取 API 返回的错误信息
            try {
                JsonNode errJson = objectMapper.readTree(errBody);
                if (errJson.has("error") && errJson.get("error").has("message")) {
                    throw new RuntimeException("AI 接口错误: " + errJson.get("error").get("message").asText());
                }
            } catch (RuntimeException re) { throw re; } catch (Exception ignored) {}
            throw new RuntimeException("AI 接口返回错误，HTTP " + response.statusCode());
        }

        JsonNode respJson = objectMapper.readTree(response.body());
        JsonNode choices = respJson.get("choices");
        if (choices == null || !choices.isArray() || choices.size() == 0) {
            throw new RuntimeException("AI 响应格式异常，无有效 choices");
        }
        JsonNode content = choices.get(0).get("message").get("content");
        if (content == null) {
            throw new RuntimeException("AI 响应中无内容");
        }
        return content.asText();
    }


    // ─── Prompt 模板 ───

    private String buildImprovePrompt(String text) {
        return """
            请帮我润色优化以下简历描述，使表达更专业、更有冲击力。

            要求：
            1. 使用行业标准的专业术语
            2. 突出量化成果（如果有数字）
            3. 使用有力的动词开头
            4. 保持原文的核心信息和长度
            5. 直接输出润色后的文本，不要添加解释

            原始文本：
            """ + text;
    }

    private String buildGeneratePrompt(String keywords, String jobTitle, String background) {
        StringBuilder sb = new StringBuilder();
        sb.append("请帮我生成专业的简历内容。\n\n");
        if (jobTitle != null && !jobTitle.isBlank()) sb.append("目标职位: ").append(jobTitle).append("\n");
        if (keywords != null && !keywords.isBlank()) sb.append("关键词/技能: ").append(keywords).append("\n");
        if (background != null && !background.isBlank()) sb.append("个人背景: ").append(background).append("\n");
        sb.append("""

            要求：
            1. 生成 2-3 条有说服力的简历要点（bullet points）
            2. 每条包含具体的技术、工具或方法
            3. 尽量体现量化成果
            4. 使用中英双语输出（先中文再英文）
            5. 直接用 Markdown 格式输出，不要添加解释
            """);
        return sb.toString();
    }

    private String buildAnalyzePrompt(String resumeContent) {
        return """
            请分析以下简历内容，并从以下维度给出具体的改进建议：

            1. **整体结构** — 简历的组织是否合理？有无遗漏的部分？
            2. **语言表达** — 用词是否专业？有无可以加强的动词或表述？
            3. **内容深度** — 描述是否足够具体？有无空洞的表述？
            4. **量化成果** — 是否有足够的数字和成果支撑？
            5. **针对性** — 如果再补充一些目标岗位的信息，可以做哪些定制化调整？

            请用中文输出分析结果，每条建议之后给出具体的修改示例。

            简历内容：
            """ + resumeContent;
    }

    private String buildTranslatePrompt(String text, String direction) {
        String lang = "en2zh".equals(direction) ? "英文翻译为中文" : "中文翻译为英文";
        return "请将以下文本从" + lang + "。要求：\n" +
               "1. 保持专业简历的语气和风格\n" +
               "2. 保持技术术语的准确性\n" +
               "3. 直接输出翻译结果，不要添加解释\n\n" +
               "原文：\n" + text;
    }


    /**
     * AI 请求配置 — 用户每次请求时提供（不存服务端）
     */
    public static class AiConfig {
        private String endpoint;
        private String apiKey;
        private String model;

        public AiConfig() {}
        public AiConfig(String endpoint, String apiKey, String model) {
            this.endpoint = endpoint; this.apiKey = apiKey; this.model = model;
        }

        public String getEndpoint() { return endpoint; }
        public void setEndpoint(String endpoint) { this.endpoint = endpoint; }
        public String getApiKey() { return apiKey; }
        public void setApiKey(String apiKey) { this.apiKey = apiKey; }
        public String getModel() { return model; }
        public void setModel(String model) { this.model = model; }
    }
}
