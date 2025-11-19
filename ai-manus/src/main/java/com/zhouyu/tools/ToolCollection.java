package com.zhouyu.tools;

import com.zhouyu.model.ToolDefinition;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 作者：IT周瑜
 * 公众号：IT周瑜
 * 微信号：it_zhouyu
 */
@Slf4j
public class ToolCollection {
    private final Map<String, Tool> tools = new HashMap<>();

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

    public ToolResult executeTool(String toolName, Map<String, Object> parameters) {
        Tool tool = tools.get(toolName);
        if (tool == null) {
            return ToolResult.error("Tool not found: " + toolName);
        }
        return tool.execute(parameters);
    }
}
