package com.xiaocang.toolcall_test.agent;

import com.xiaocang.toolcall_test.config.AgentProperties;
import com.xiaocang.toolcall_test.dto.ChatMessage;
import com.xiaocang.toolcall_test.dto.OllamaRequest;
import com.xiaocang.toolcall_test.dto.OllamaResponse;
import com.xiaocang.toolcall_test.ollama.OllamaClient;
import com.xiaocang.toolcall_test.tool.Tool;
import com.xiaocang.toolcall_test.tool.ToolInvoker;
import com.xiaocang.toolcall_test.tool.ToolRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

/**
 * 函数调用主循环。
 *
 * <pre>
 * 用户输入
 *   └─► 调 Ollama（带 tools）
 *         ├─ 模型直接回 content ──────────► 返回给用户
 *         └─ 模型要求 tool_call
 *               ├─ ToolInvoker 执行（校验失败/异常都转成字符串）
 *               ├─ 结果作为 role=tool 消息追加到历史
 *               └─ 回到顶部再调一次 Ollama
 * </pre>
 *
 * 最大轮次 maxRounds 防止模型死循环调工具。
 */
@Service
public class AgentService {

    private static final Logger log = LoggerFactory.getLogger(AgentService.class);

    private static final String SYSTEM_PROMPT = """
            你是企业内部个人助手，可以调用工具完成任务。
            规则：
            1. 需要查数据/做计算时，必须调用工具，不要编造结果。
            2. 如果工具返回"参数校验失败"或"参数错误"，请根据错误信息修正参数后重新调用同名工具。
            3. 工具结果返回后，用自然语言总结给用户，不要原样堆砌 JSON。
            4. 不要同时发起多个互相矛盾的工具调用。
            """;

    private final OllamaClient ollama;
    private final ToolRegistry registry;
    private final ToolInvoker invoker;
    private final AgentProperties props;

    public AgentService(OllamaClient ollama,
                        ToolRegistry registry,
                        ToolInvoker invoker,
                        AgentProperties props) {
        this.ollama = ollama;
        this.registry = registry;
        this.invoker = invoker;
        this.props = props;
    }

    public String chat(String userInput) throws Exception {
        List<ChatMessage> history = new ArrayList<>();
        history.add(ChatMessage.system(SYSTEM_PROMPT));
        history.add(ChatMessage.user(userInput));

        List<java.util.Map<String, Object>> tools = registry.toOllamaTools();
        log.info("可用工具: {}", tools.stream().map(t -> ((java.util.Map<?, ?>) t.get("function")).get("name")).toList());

        for (int round = 1; round <= props.maxRounds(); round++) {
            OllamaResponse resp = ollama.chat(OllamaRequest.of(props.model(), history, tools));
            ChatMessage msg = resp.message();

            // 把 assistant 的这一轮（可能带 tool_calls）加进历史
            history.add(msg);

            if (msg.toolCalls() == null || msg.toolCalls().isEmpty()) {
                String content = msg.content();
                log.info("第 {} 轮，模型直接回复: {}", round, content);
                return content == null ? "" : content;
            }

            // 模型要求调用工具：逐个执行，结果追加为 role=tool 消息
            for (ChatMessage.ToolCall call : msg.toolCalls()) {
                String name = call.function().name();
                Object args = call.function().arguments();
                log.info("第 {} 轮，模型调用工具 {} args={}", round, name, args);

                String result;
                try {
                    Tool tool = registry.require(name);
                    result = invoker.invoke(tool, args);
                } catch (NoSuchElementException e) {
                    result = "错误：" + e.getMessage();
                }
                log.info("  -> 工具结果: {}", result);

                // 关键：错误也作为 tool 消息喂回去，模型下一轮会自己改
                history.add(ChatMessage.toolResult(name, result));
            }
            // 继续下一轮，让模型看到工具结果后作答
        }
        return "已达到最大工具调用轮次 " + props.maxRounds() + "，任务未完成。";
    }
}
