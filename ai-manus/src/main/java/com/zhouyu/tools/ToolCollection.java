package com.zhouyu.tools;

import com.zhouyu.model.Message;
import com.zhouyu.model.RelevanceFilter;
import com.zhouyu.model.ToolDefinition;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 作者：IT周瑜
 * 公众号：IT周瑜
 * 微信号：it_zhouyu
 */
@Slf4j
public class ToolCollection {
    private final Map<String, Tool> tools = new HashMap<>();
    @Setter
    private RelevanceFilter relevanceFilter;

    private double relevanceThreshold = 0.3;

    public void addTool(Tool tool) {
        tools.put(tool.getName(), tool);
    }

    public List<ToolDefinition> getToolDefinitions() {
        List<ToolDefinition> definitions = new ArrayList<>();
        for (Tool tool : tools.values()) {
            definitions.add(tool.toDefinition());
        }
        return definitions;
    }

    public List<ToolDefinition> getRelevantToolDefinitions(String query) {
        if (relevanceFilter == null || query == null || query.trim().isEmpty()) {
            return getToolDefinitions();
        }

        List<ToolDefinition> allTools = getToolDefinitions();

        // 为每个工具计算相关性得分
        List<ToolScore> scoredTools = allTools.stream()
                .map(tool -> {
                    String toolText = tool.getName() + " " + tool.getDescription();
                    // 将工具信息转换为Message格式进行相关性计算
                    Message message = Message.assistantMessage(toolText);
                    double relevance = relevanceFilter.calculateRelevance(message, query);
                    return new ToolScore(tool, relevance);
                })
                .filter(ts -> ts.score >= relevanceThreshold) // 过滤低相关性工具
                .sorted((a, b) -> Double.compare(b.score, a.score)) // 按相关性排序
                .toList();

        List<ToolDefinition> relevantTools = scoredTools.stream()
                .map(ts -> ts.tool)
                .collect(Collectors.toList());

        log.debug("工具相关性过滤: 查询='{}', 原始工具数={}, 过滤后工具数={}",
                query, allTools.size(), relevantTools.size());

        // 如果过滤后没有相关工具，返回所有工具以避免完全无法使用
        if (relevantTools.isEmpty()) {
            log.warn("没有找到相关的工具，返回所有工具");
            return allTools;
        }

        return relevantTools;
    }

    public ToolResult executeTool(String toolName, Map<String, Object> parameters) {
        Tool tool = tools.get(toolName);
        if (tool == null) {
            return ToolResult.error("Tool not found: " + toolName);
        }
        return tool.execute(parameters);
    }

    private static class ToolScore {
        final ToolDefinition tool;
        final double score;

        ToolScore(ToolDefinition tool, double score) {
            this.tool = tool;
            this.score = score;
        }
    }
}
