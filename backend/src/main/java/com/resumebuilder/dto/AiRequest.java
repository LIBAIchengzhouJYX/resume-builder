package com.resumebuilder.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AiRequest {

    public enum Action {
        IMPROVE,    // 润色优化
        GENERATE,   // 根据关键词生成
        ANALYZE,    // 分析简历给建议
        TRANSLATE   // 中英互译
    }

    private Action action;

    // 输入文本：IMPROVE/TRANSLATE 用
    private String text;

    // 生成模式：需要的关键词/岗位/背景
    private String keywords;
    private String jobTitle;
    private String background;

    // 全文分析模式：传整个简历 JSON
    private String resumeContent;

    // 语言方向：zh2en / en2zh
    private String translateDirection;
}
