package com.xiaocang.toolcall_test.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.List;
import java.util.Map;

/**
 * POST /api/chat 请求体。
 *
 * @param tools Ollama 的工具描述，每项形如：
 *              {"type":"function","function":{"name":...,"description":...,"parameters":{...JSON Schema...}}}
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record OllamaRequest(
        String model,
        List<ChatMessage> messages,
        List<Map<String, Object>> tools,
        boolean stream
) {
    public static OllamaRequest of(String model, List<ChatMessage> messages, List<Map<String, Object>> tools) {
        return new OllamaRequest(model, messages, tools, false);
    }
}
