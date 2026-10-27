package com.xiaocang.toolcall_test.ollama;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.xiaocang.toolcall_test.config.AgentProperties;
import com.xiaocang.toolcall_test.dto.OllamaRequest;
import com.xiaocang.toolcall_test.dto.OllamaResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;

/**
 * 调本地 Ollama 的 /api/chat。
 * 用 JDK HttpClient，避免再引 RestTemplate / WebClient。
 */
@Component
public class OllamaClient {

    private static final Logger log = LoggerFactory.getLogger(OllamaClient.class);

    private final HttpClient http = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(5))
            .build();

    private final ObjectMapper mapper;
    private final AgentProperties props;

    public OllamaClient(ObjectMapper mapper, AgentProperties props) {
        this.mapper = mapper;
        this.props = props;
    }

    public OllamaResponse chat(OllamaRequest request) throws IOException, InterruptedException {
        String body = mapper.writeValueAsString(request);
        if (log.isDebugEnabled()) {
            log.debug("-> Ollama: {}", body);
        }

        HttpRequest httpReq = HttpRequest.newBuilder()
                .uri(URI.create(props.baseUrl() + "/api/chat"))
                .header("Content-Type", "application/json")
                .timeout(Duration.ofSeconds(120))
                .POST(HttpRequest.BodyPublishers.ofString(body, StandardCharsets.UTF_8))
                .build();

        HttpResponse<String> resp = http.send(httpReq,
                HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));

        if (resp.statusCode() != 200) {
            throw new IOException("Ollama HTTP " + resp.statusCode() + ": " + resp.body());
        }
        return mapper.readValue(resp.body(), OllamaResponse.class);
    }
}
