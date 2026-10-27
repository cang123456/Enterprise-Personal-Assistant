package com.xiaocang.toolcall_test.dto;

/**
 * POST /api/chat 响应体（stream=false 时是一整个 JSON）。
 */
public record OllamaResponse(
        ChatMessage message,
        boolean done
) {}
