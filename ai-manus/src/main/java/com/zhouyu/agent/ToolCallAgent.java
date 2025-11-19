package com.zhouyu.agent;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zhouyu.model.*;
import com.zhouyu.tools.ToolCollection;
import com.zhouyu.tools.ToolResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * 作者：IT周瑜
 * 公众号：IT周瑜
 * 微信号：it_zhouyu
 */

public class ToolCallAgent extends BaseAgent {

    private static final Logger logger = LoggerFactory.getLogger(ToolCallAgent.class);

    protected ToolCollection toolCollection;
    private ObjectMapper objectMapper;
    protected OpenAIClient openAIClient;

    public ToolCallAgent(OpenAIClient openAIClient, ToolCollection toolCollection, String systemPrompt) {
        super(systemPrompt);
        this.openAIClient = openAIClient;
        this.toolCollection = toolCollection;
        this.objectMapper = new ObjectMapper();
    }

    @Override
    protected StepResult step(String currentQuery) {

//        List<Message> contextMessages = memory.getMessages();
        List<Message> contextMessages = memory.getMessages(currentQuery);

//        List<ToolDefinition> toolDefinitions = toolCollection.getToolDefinitions();
        List<ToolDefinition> toolDefinitions = toolCollection.getRelevantToolDefinitions(currentQuery);

        ModelResponse modelResponse = openAIClient.chat(contextMessages, toolDefinitions);

        // 大模型需要调用工具，执行完工具会直接返回
        if (modelResponse.hasToolCalls()) {
            // 将大模型返回的工具调用请求转成assistantMessage，并存到memory中
            Message assistantMessage = Message.assistantMessage(modelResponse.getContent());
            assistantMessage.setToolCalls(convertToToolCalls(modelResponse.getToolCalls()));
            memory.addMessage(assistantMessage);

            // 执行工具,得到工具执行结果并返回
            return handleToolCalls(modelResponse.getToolCalls());
        }

        // 大模型不需要调用工具
        if (modelResponse.getContent() != null && !modelResponse.getContent().isBlank()) {
            Message message = Message.assistantMessage(modelResponse.getContent());
            memory.addMessage(message);
        }

        // 大模型认为任务完成了，可以结束了
        if (modelResponse.getFinishReason().equals("stop")) {
            return StepResult.builder().shouldContinue(false).output("大模型认为任务已经执行结束").build();
        }

        // 否则继续执行
        return StepResult.builder().shouldContinue(true).output(modelResponse.getContent()).build();
    }

    private List<ToolCall> convertToToolCalls(List<Object> toolCallObjects) {
        return toolCallObjects.stream().map(obj -> {
            JsonNode node = objectMapper.valueToTree(obj);
            String id = node.get("id").asText();
            String type = node.has("type") ? node.get("type").asText() : "function";
            JsonNode functionNode = node.get("function");
            String name = functionNode.get("name").asText();
            String arguments = functionNode.get("arguments").asText();

            Function function = new Function(name, arguments);
            return new ToolCall(id, type, function);
        }).collect(Collectors.toList());
    }

    private StepResult handleToolCalls(List<Object> toolCalls) {
        StringBuilder allResults = new StringBuilder();
        for (Object toolCallObj : toolCalls) {
            try {
                JsonNode toolCallNode = objectMapper.valueToTree(toolCallObj);
                String toolCallId = toolCallNode.get("id").asText();
                String toolName = toolCallNode.get("function").get("name").asText();
                String argumentsJson = toolCallNode.get("function").get("arguments").asText();

                logger.info("执行工具: {}, toolCallId: {}", toolName, toolCallId);

                // 执行工具
                Map<String, Object> arguments = objectMapper.readValue(argumentsJson, Map.class);
                ToolResult result = toolCollection.executeTool(toolName, arguments);

                String resultContent;
                if (result.hasError()) {
                    resultContent = "Error: " + result.getError();
                    logger.warn("工具 {} 执行失败: {}", toolName, result.getError());
                } else {
                    resultContent = result.getOutput() != null ? result.getOutput().toString() : "Success";
                    logger.info("工具 {} 执行成功, 结果为: {}", toolName, resultContent);
                }

                // 把工具执行结果转成toolMessage并添加到memory中
                Message toolMessage = Message.toolMessage(resultContent, toolName, toolCallId, result.getBase64Image());
                memory.addMessage(toolMessage);

                allResults.append(toolName).append(": ").append(resultContent).append("\n");
            } catch (Exception e) {
                logger.error("工具执行失败", e);
                String errorMsg = "工具执行失败: " + e.getMessage();
                Message errorMessage = Message.toolMessage(errorMsg, "unknown", UUID.randomUUID().toString());
                memory.addMessage(errorMessage);
            }
        }

        return StepResult.builder().shouldContinue(true).output(allResults.toString()).build();
    }
}
