package com.xiaocang.toolcall_test.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * 与 Ollama /api/chat 交互的消息结构。
 * <p>
 * 注意：Ollama 返回的 assistant 消息里，tool_calls[].function.arguments 可能是：
 *   - 一个 JSON object（绝大多数情况）
 *   - 偶尔被某些模型（如 qwen2.5）压成一个 JSON 字符串
 * 所以这里把 arguments 声明成 Object，由 ToolInvoker 做兜底解析。
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ChatMessage(
        String role,
        String content,
        String name,
        @JsonProperty("tool_calls")
        List<ToolCall> toolCalls
) {

    public record ToolCall(@JsonProperty("function") FunctionCall function) {}

    public record FunctionCall(String name, Object arguments) {}

    public static ChatMessage system(String content) {
        return new ChatMessage("system", content, null, null);
    }

    public static ChatMessage user(String content) {
        return new ChatMessage("user", content, null, null);
    }

    public static ChatMessage assistant(String content, List<ToolCall> toolCalls) {
        return new ChatMessage("assistant", content, null, toolCalls);
    }

    /** 工具执行结果，作为 role=tool 的消息喂回模型。 */
    public static ChatMessage toolResult(String toolName, String content) {
        return new ChatMessage("tool", content, toolName, null);
    }
}
