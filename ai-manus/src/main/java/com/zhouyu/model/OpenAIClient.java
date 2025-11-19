package com.zhouyu.model;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 作者：IT周瑜
 * 公众号：IT周瑜
 * 微信号：it_zhouyu
 */
public class OpenAIClient {
    private static final Logger logger = LoggerFactory.getLogger(OpenAIClient.class);
    private static final MediaType JSON = MediaType.get("application/json; charset=utf-8");


    private final ModelConfig modelConfig;
    private final OkHttpClient httpClient;
    private final ObjectMapper objectMapper;

    public OpenAIClient(ModelConfig modelConfig) {
        this.modelConfig = modelConfig;
        this.objectMapper = new ObjectMapper();
        this.httpClient = new OkHttpClient.Builder()
                .connectTimeout(Duration.ofSeconds(30))
                .readTimeout(Duration.ofMinutes(5))
                .writeTimeout(Duration.ofMinutes(5))
                .build();
    }

    public ModelResponse chat(List<Message> messages) {
        return chat(messages, null);
    }

    public ModelResponse chat(List<Message> messages, List<ToolDefinition> tools) {
        try {
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("model", modelConfig.getModel());
            requestBody.put("messages", convertMessagesToApiFormat(messages));

            if (tools != null && !tools.isEmpty()) {
                requestBody.put("tools", convertToolsToApiFormat(tools));
            }

            String jsonBody = objectMapper.writeValueAsString(requestBody);

            Request request = new Request.Builder()
                    .url(modelConfig.getBaseUrl() + "/chat/completions")
                    .post(RequestBody.create(jsonBody, JSON))
                    .addHeader("Authorization", "Bearer " + modelConfig.getApiKey())
                    .addHeader("Content-Type", "application/json")
                    .build();

            try (Response response = httpClient.newCall(request).execute()) {
                if (!response.isSuccessful()) {
                    throw new RuntimeException("LLM API调用失败：" + response);
                }

                return parseResponse(response.body().string());
            }
        } catch (Exception e) {
            logger.error("LLM API调用期间发生错误", e);
            throw new RuntimeException("LLM API调用失败", e);
        }
    }

    private List<Map<String, Object>> convertMessagesToApiFormat(List<Message> messages) {
        List<Map<String, Object>> apiMessages = new ArrayList<>();
        
        for (Message message : messages) {
            Map<String, Object> apiMessage = new HashMap<>();
            apiMessage.put("role", message.getRole().getValue());

            if (message.getContent() != null) {
                if (message.getBase64Image() != null) {
                    // 处理多模态内容
                    List<Map<String, Object>> content = new ArrayList<>();
                    content.add(Map.of("type", "text", "text", message.getContent()));
                    content.add(Map.of(
                            "type", "image_url",
                            "image_url", Map.of("url", "data:image/jpeg;base64," + message.getBase64Image())
                    ));
                    apiMessage.put("content", content);
                } else {
                    apiMessage.put("content", message.getContent());
                }
            }
            
            if (message.getToolCalls() != null) {
                apiMessage.put("tool_calls", message.getToolCalls());
            }
            
            if (message.getName() != null) {
                apiMessage.put("name", message.getName());
            }
            
            if (message.getToolCallId() != null) {
                apiMessage.put("tool_call_id", message.getToolCallId());
            }
            
            apiMessages.add(apiMessage);
        }
        
        return apiMessages;
    }

    private List<Map<String, Object>> convertToolsToApiFormat(List<ToolDefinition> tools) {
        List<Map<String, Object>> apiTools = new ArrayList<>();
        
        for (ToolDefinition tool : tools) {
            Map<String, Object> apiTool = new HashMap<>();
            apiTool.put("type", "function");
            
            Map<String, Object> function = new HashMap<>();
            function.put("name", tool.getName());
            function.put("description", tool.getDescription());
            function.put("parameters", tool.getParameters());
            
            apiTool.put("function", function);
            apiTools.add(apiTool);
        }
        
        return apiTools;
    }

    private ModelResponse parseResponse(String responseBody) throws IOException {
        JsonNode jsonResponse = objectMapper.readTree(responseBody);
        
        JsonNode choice = jsonResponse.get("choices").get(0);
        JsonNode message = choice.get("message");
        
        String content = message.has("content") && !message.get("content").isNull() 
                ? message.get("content").asText() 
                : null;
        
        List<Object> toolCalls = new ArrayList<>();
        if (message.has("tool_calls")) {
            JsonNode toolCallsNode = message.get("tool_calls");
            for (JsonNode toolCall : toolCallsNode) {
                toolCalls.add(objectMapper.convertValue(toolCall, Object.class));
            }
        }
        
        String finishReason = choice.get("finish_reason").asText();
        
        return new ModelResponse(content, toolCalls, finishReason);
    }
}