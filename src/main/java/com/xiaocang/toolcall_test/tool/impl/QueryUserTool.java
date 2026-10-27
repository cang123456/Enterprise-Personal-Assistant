package com.xiaocang.toolcall_test.tool.impl;

import com.xiaocang.toolcall_test.tool.Tool;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.extern.slf4j.Slf4j;
import org.apache.juli.logging.LogFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * 对应笔记里的 query_user 例子：
 *   keyword  必填，最长 32
 *   pageNum  1~100
 *   status   normal | disabled
 */
@Component

public class QueryUserTool implements Tool {

    private static final Logger log = LoggerFactory.getLogger(QueryUserTool.class);
    @Override
    public String name() {
        return "query_user";
    }

    @Override
    public String description() {
        return "按关键字查询企业内部用户列表。keyword 为用户名/工号模糊搜索；"
                + "pageNum 从 1 开始；status 可按 normal/disabled 过滤。";
    }

    @Override
    public Map<String, Object> parametersSchema() {
        return Map.of(
                "type", "object",
                "properties", Map.of(
                        "keyword", Map.of("type", "string", "maxLength", 32,
                                "description", "搜索关键字（用户名/工号），必填"),
                        "pageNum", Map.of("type", "integer", "minimum", 1, "maximum", 100,
                                "description", "页码，默认 1"),
                        "status", Map.of("type", "string", "enum", List.of("normal", "disabled"),
                                "description", "状态过滤，可选")
                ),
                "required", List.of("keyword")
        );
    }

    @Override
    public Class<?> paramType() {
        return QueryUserArgs.class;
    }

    @Override
    public Object execute(Object params) {
        QueryUserArgs args = (QueryUserArgs) params;
        // 业务层：这里本来该查库。演示用返回一条模拟数据。
        // 业务校验（对应 @model_validator）：比如 keyword 不能是管理员保留名
        log.info("Tool查询用户{}",args.keyword());
        if ("root".equalsIgnoreCase(args.keyword())) {
            throw new IllegalArgumentException("不允许查询 root 账号");
        }
        int page = args.pageNum() == null ? 1 : args.pageNum();
        String status = args.status() == null ? "normal" : args.status();

        return Map.of(
                "total", 1,
                "pageNum", page,
                "list", List.of(
                        Map.of("id", 1001, "name", args.keyword(), "status", status)
                )
        );
    }

    /** 参数 record —— 几乎就是 Pydantic 的 BaseModel。 */
    public record QueryUserArgs(
            @NotBlank(message = "不能为空")
            @Size(max = 32, message = "最长 32 字")
            String keyword,

            @Min(value = 1, message = "从 1 开始")
            @Max(value = 100, message = "最多 100")
            Integer pageNum,

            @Pattern(regexp = "^(normal|disabled)$", message = "只能是 normal 或 disabled")
            String status
    ) {}
}
