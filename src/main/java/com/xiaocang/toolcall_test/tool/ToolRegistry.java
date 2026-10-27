package com.xiaocang.toolcall_test.tool;

import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

/**
 * Spring 会把所有 Tool 实现注入成 List，这里做一个按名索引，
 * 顺便把工具描述转成 Ollama 需要的 tools 数组格式。
 */
@org.springframework.stereotype.Component
public class ToolRegistry {

    private final Map<String, Tool> byName;

    public ToolRegistry(List<Tool> tools) {
        this.byName = tools.stream()
                .collect(java.util.stream.Collectors.toUnmodifiableMap(Tool::name, t -> t));
    }

    public Tool require(String name) {
        Tool t = byName.get(name);
        if (t == null) {
            throw new NoSuchElementException("未注册的工具: " + name);
        }
        return t;
    }

    /**
     * 转成 Ollama /api/chat 的 tools 字段：
     * [{"type":"function","function":{"name":...,"description":...,"parameters":{...}}}]
     */
    public List<Map<String, Object>> toOllamaTools() {
        return byName.values().stream()
                .map(t -> Map.<String, Object>of(
                        "type", "function",
                        "function", Map.of(
                                "name", t.name(),
                                "description", t.description(),
                                "parameters", t.parametersSchema()
                        )
                ))
                .toList();
    }
}
