package com.xiaocang.toolcall_test.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * agent.ollama.base-url=http://localhost:11434
 * agent.ollama.model=qwen2.5
 * agent.ollama.max-rounds=8
 */
@ConfigurationProperties(prefix = "agent.ollama")
public record AgentProperties(
        String baseUrl,
        String model,
        int maxRounds
) {
    public AgentProperties {
        if (baseUrl == null || baseUrl.isBlank()) baseUrl = "http://localhost:11434";
        if (model == null || model.isBlank()) model = "qwen2.5";
        if (maxRounds <= 0) maxRounds = 8;
    }
}
