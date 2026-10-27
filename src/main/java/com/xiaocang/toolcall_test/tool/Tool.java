package com.xiaocang.toolcall_test.tool;

import java.util.Map;

/**
 * 一个可被模型调用的工具。
 * <p>
 * 三层校验对应 Pydantic：
 *   1. 结构层 —— Jackson 把 Map 反序列化成 paramType() 对应的 record
 *   2. 约束层 —— ToolInvoker 跑 Jakarta Bean Validation（@NotBlank/@Min/@Pattern…）
 *   3. 业务层 —— execute() 里自己查库/查存在性（对应 @model_validator）
 */
public interface Tool {

    /** 工具名，模型用这个名字调用。snake_case。 */
    String name();

    /** 给模型看的描述，写清楚什么时候该用这个工具。 */
    String description();

    /**
     * 参数的 JSON Schema（Ollama / OpenAI function calling 的 parameters 字段）。
     * 一般手写 Map.of(...) 即可，和 record 字段保持一致。
     */
    Map<String, Object> parametersSchema();

    /** 参数 record 类型，Jackson 反序列化的目标。 */
    Class<?> paramType();

    /**
     * 执行业务逻辑。返回值会被序列化成 JSON 字符串喂回模型。
     * 这里可以抛运行时异常，ToolInvoker 会捕获并转成错误消息。
     */
    Object execute(Object params);
}
