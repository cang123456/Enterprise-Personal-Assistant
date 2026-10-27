package com.xiaocang.toolcall_test.tool;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 工具执行器：对应笔记里的 AiToolInvoker。
 * <p>
 * 关键设计：校验/反序列化/执行的错误一律转成字符串返回，
 * 由 AgentService 包成 role=tool 的消息喂回模型，让模型自己改参数重试。
 * 而不是抛异常把整个对话炸掉。
 */
@Component
public class ToolInvoker {

    private static final Logger log = LoggerFactory.getLogger(ToolInvoker.class);

    private final Validator validator;
    private final ObjectMapper mapper;

    public ToolInvoker(Validator validator, ObjectMapper mapper) {
        this.validator = validator;
        this.mapper = mapper;
    }

    /**
     * @param tool    目标工具
     * @param rawArgs 模型给的原始 arguments，可能是 Map，也可能是 String（qwen2.5 偶尔压成字符串）
     * @return 工具结果（JSON 字符串）或错误消息（直接喂回模型）
     */
    public String invoke(Tool tool, Object rawArgs) {
        // 1. 兜底：把 arguments 统一成 Map
        Map<String, Object> args;
        try {
            args = toMap(rawArgs);
        } catch (Exception e) {
            return "参数格式错误：arguments 既不是对象也不是合法 JSON 字符串。原始值=" + rawArgs;
        }

        // 2. 结构层：Map -> record（FAIL_ON_UNKNOWN_PROPERTIES 已关，模型多塞字段不炸）
        Object params;
        try {
            params = mapper.convertValue(args, tool.paramType());
        } catch (IllegalArgumentException e) {
            return "参数反序列化失败：" + e.getMessage() + "。请按工具 schema 修正参数后重试。";
        }

        // 3. 约束层：Jakarta Bean Validation
        Set<ConstraintViolation<Object>> violations = validator.validate(params);
        if (!violations.isEmpty()) {
            String msg = violations.stream()
                    .map(v -> v.getPropertyPath() + " " + v.getMessage())
                    .collect(Collectors.joining("；"));
            return "参数校验失败：" + msg + "。请修正参数后重新调用。";
        }

        // 4. 业务层：执行
        try {
            Object result = tool.execute(params);
            return mapper.writeValueAsString(result);
        } catch (Exception e) {
            log.warn("工具 {} 执行异常", tool.name(), e);
            return "工具执行异常：" + e.getMessage();
        }
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> toMap(Object raw) throws Exception {
        if (raw == null) {
            return Map.of();
        }
        if (raw instanceof Map<?, ?> m) {
            return (Map<String, Object>) m;
        }
        if (raw instanceof String s) {
            String trimmed = s.trim();
            if (trimmed.isEmpty()) {
                return Map.of();
            }
            // 模型偶尔会把整个 JSON 塞成字符串，这里 parse 回来
            return mapper.readValue(trimmed, Map.class);
        }
        return mapper.convertValue(raw, Map.class);
    }
}
