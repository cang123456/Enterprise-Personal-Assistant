package com.xiaocang.toolcall_test.agent;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/chat")
public class ChatController {

    private final AgentService agent;

    public ChatController(AgentService agent) {
        this.agent = agent;
    }

    @PostMapping
    public Map<String, String> chat(@RequestBody Map<String, String> body) throws Exception {
        String message = body.getOrDefault("message", "").trim();
        if (message.isEmpty()) {
            return Map.of("reply", "请输入内容");
        }
        return Map.of("reply", agent.chat(message));
    }
}
