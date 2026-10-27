package com.xiaocang.toolcall_test.tool.impl;

import com.xiaocang.toolcall_test.tool.Tool;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * 第二个示例工具：四则运算。
 * 用来演示模型在参数错（比如 op 给了 "power"）时，被错误消息纠正后重试。
 */
@Component
public class CalculatorTool implements Tool {

    @Override
    public String name() {
        return "calculator";
    }

    @Override
    public String description() {
        return "对两个数字做四则运算。op 只能是 add/sub/mul/div。";
    }

    @Override
    public Map<String, Object> parametersSchema() {
        return Map.of(
                "type", "object",
                "properties", Map.of(
                        "a", Map.of("type", "number", "description", "左操作数"),
                        "b", Map.of("type", "number", "description", "右操作数"),
                        "op", Map.of("type", "string", "enum", List.of("add", "sub", "mul", "div"),
                                "description", "运算：加/减/乘/除")
                ),
                "required", List.of("a", "b", "op")
        );
    }

    @Override
    public Class<?> paramType() {
        return CalcArgs.class;
    }

    @Override
    public Object execute(Object params) {
        CalcArgs args = (CalcArgs) params;
        double a = args.a();
        double b = args.b();
        double r = switch (args.op()) {
            case "add" -> a + b;
            case "sub" -> a - b;
            case "mul" -> a * b;
            case "div" -> {
                if (b == 0) throw new IllegalArgumentException("除数不能为 0");
                yield a / b;
            }
            default -> throw new IllegalStateException("未知 op: " + args.op());
        };
        return Map.of("expression", a + " " + args.op() + " " + b, "result", r);
    }

    public record CalcArgs(
            @NotNull(message = "a 不能为空")
            Double a,

            @NotNull(message = "b 不能为空")
            Double b,

            @NotBlank(message = "op 不能为空")
            @Pattern(regexp = "^(add|sub|mul|div)$", message = "op 只能是 add/sub/mul/div")
            String op
    ) {}
}
