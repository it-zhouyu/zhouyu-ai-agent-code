package com.zhouyu.model;

import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 作者：IT周瑜
 * 公众号：IT周瑜
 * 微信号：it_zhouyu
 */
@Slf4j
public class LLMRelevanceFilter implements RelevanceFilter {
    
    private final OpenAIClient openAIClient;
    
    public LLMRelevanceFilter(OpenAIClient openAIClient) {
        this.openAIClient = openAIClient;
    }
    
    @Override
    public List<Message> filter(List<Message> messages, String currentQuery, int maxMessages) {
        if (messages == null || messages.isEmpty()) {
            return new ArrayList<>();
        }
        
        // 如果消息数量不超过最大限制，直接返回
        if (messages.size() <= maxMessages) {
            return new ArrayList<>(messages);
        }
        
        // 分离系统消息和其他消息
        List<Message> systemMessages = messages.stream()
                .filter(msg -> msg.getRole() == Role.SYSTEM)
                .toList();
        
        List<Message> nonSystemMessages = messages.stream()
                .filter(msg -> msg.getRole() != Role.SYSTEM)
                .toList();
        
        // 为非系统消息计算相关性得分
        List<MessageScore> scoredMessages = nonSystemMessages.stream()
                .map(msg -> new MessageScore(msg, calculateRelevance(msg, currentQuery)))
                .sorted((a, b) -> Double.compare(b.score, a.score))
                .toList();
        
        // 保留系统消息和得分最高的非系统消息
        List<Message> result = new ArrayList<>(systemMessages);
        int remainingSlots = maxMessages - systemMessages.size();
        
        if (remainingSlots > 0) {
            result.addAll(scoredMessages.stream()
                    .limit(remainingSlots)
                    .map(ms -> ms.message)
                    .toList());
        }
        
        log.debug("过滤前消息数量: {}, 过滤后消息数量: {}", messages.size(), result.size());
        return result;
    }
    
    @Override
    public double calculateRelevance(Message message, String query) {
        if (message == null || message.getContent() == null || query == null) {
            return 0.0;
        }
        
        try {
            // 构建用于相关性评估的提示
            String relevancePrompt = buildRelevancePrompt(message.getContent(), query);
            
            List<Message> promptMessages = Arrays.asList(
                Message.systemMessage("你是一个专业的语义相关性评估专家。请严格按照要求评估消息的相关性，只返回数字评分。"),
                Message.userMessage(relevancePrompt)
            );
            
            // 调用LLM进行相关性评估
            ModelResponse response = openAIClient.chat(promptMessages);
            String content = response.getContent();
            
            if (content != null) {
                // 解析LLM返回的相关性得分
                double relevance = parseRelevanceScore(content);;
                
                return Math.max(0.0, Math.min(1.0, relevance));
            }
            
        } catch (Exception e) {
            log.warn("LLM相关性评估失败，使用默认评分", e);
        }
        
        return 0.0;
    }
    
    private String buildRelevancePrompt(String messageContent, String query) {
        return String.format(
            "请评估以下消息内容与查询的相关性，返回0.0到1.0之间的数字评分：\n\n" +
            "查询：%s\n\n" +
            "消息内容：%s\n\n" +
            "评估标准：\n" +
            "1.0 - 高度相关：消息直接回答查询或包含查询的核心信息\n" +
            "0.7-0.9 - 相关：消息与查询主题相关，包含有用信息\n" +
            "0.4-0.6 - 部分相关：消息与查询有一定关联，但不是核心相关\n" +
            "0.1-0.3 - 微弱相关：消息与查询只有很少关联\n" +
            "0.0 - 不相关：消息与查询完全无关\n\n" +
            "请只返回数字评分，不要包含其他文字说明：",
            query, messageContent
        );
    }
    
    private double parseRelevanceScore(String content) {
        if (content == null || content.trim().isEmpty()) {
            return 0.0;
        }
        
        // 提取内容中的数字
        String trimmed = content.trim();
        
        // 尝试直接解析第一个数字
        try {
            // 提取第一个数字（可能包含小数点）
            String numberStr = trimmed.replaceAll("[^0-9.].*", "");
            if (!numberStr.isEmpty()) {
                double score = Double.parseDouble(numberStr);
                // 确保分数在有效范围内
                return Math.max(0.0, Math.min(1.0, score));
            }
        } catch (NumberFormatException e) {
            log.warn("无法解析LLM返回的相关性得分: {}", content);
        }
        
        // 如果无法解析数字，尝试从文本中推断
        String lowerContent = content.toLowerCase();
        if (lowerContent.contains("高度相关") || lowerContent.contains("very relevant")) {
            return 0.9;
        } else if (lowerContent.contains("相关") || lowerContent.contains("relevant")) {
            return 0.7;
        } else if (lowerContent.contains("部分") || lowerContent.contains("partial")) {
            return 0.5;
        } else if (lowerContent.contains("微弱") || lowerContent.contains("weak")) {
            return 0.2;
        } else if (lowerContent.contains("不相关") || lowerContent.contains("not relevant")) {
            return 0.0;
        }
        
        return 0.0;
    }

    private static class MessageScore {
        final Message message;
        final double score;
        
        MessageScore(Message message, double score) {
            this.message = message;
            this.score = score;
        }
    }
}