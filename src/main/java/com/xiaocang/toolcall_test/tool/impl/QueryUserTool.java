package com.xiaocang.toolcall_test.tool.impl;

import com.xiaocang.toolcall_test.entity.User;
import com.xiaocang.toolcall_test.mapper.UserMapper;
import com.xiaocang.toolcall_test.tool.Tool;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * 对应用户笔记里的 query_user 例子：
 *   keyword  必填，最长 32
 *   pageNum  1~100（页码）
 *   status   normal | disabled
 *
 * 这次是真实查库（sys_user 表），不再是内存模拟。
 */
@Component
public class QueryUserTool implements Tool {

    /** 每页大小，工具 schema 里不暴露给模型，固定 */
    private static final int PAGE_SIZE = 10;

    private final UserMapper userMapper;

    public QueryUserTool(UserMapper userMapper) {
        this.userMapper = userMapper;
    }

    @Override
    public String name() {
        return "query_user";
    }

    @Override
    public String description() {
        return "按关键字查询企业内部用户列表（来自数据库 sys_user 表）。"
                + "keyword 为用户名/昵称模糊搜索；pageNum 从 1 开始，每页 10 条；"
                + "status 可按 normal/disabled 过滤。";
    }

    @Override
    public Map<String, Object> parametersSchema() {
        return Map.of(
                "type", "object",
                "properties", Map.of(
                        "keyword", Map.of("type", "string", "maxLength", 32,
                                "description", "搜索关键字（登录名/昵称），必填"),
                        "pageNum", Map.of("type", "integer", "minimum", 1, "maximum", 100,
                                "description", "页码，从 1 开始，默认 1"),
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

        // 业务层校验（对应 @model_validator）：禁止查 root
        if ("root".equalsIgnoreCase(args.keyword())) {
            throw new IllegalArgumentException("不允许查询 root 账号");
        }

        int pageNum = args.pageNum() == null ? 1 : args.pageNum();
        int offset = (pageNum - 1) * PAGE_SIZE;
        String status = args.status();   // 可能为 null

        long total = userMapper.countUsers(args.keyword(), status);
        List<User> list = userMapper.searchUsers(args.keyword(), status, offset, PAGE_SIZE);

        return Map.of(
                "total", total,
                "pageNum", pageNum,
                "pageSize", PAGE_SIZE,
                "list", list
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
