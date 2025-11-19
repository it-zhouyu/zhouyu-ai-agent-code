package com.zhouyu.tools;

import com.zhouyu.model.ToolDefinition;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * 作者：IT周瑜
 * 公众号：IT周瑜
 * 微信号：it_zhouyu
 */
public interface Tool {
    String getName();
    String getDescription();
    Map<String, Object> getParametersSchema();
    ToolResult execute(Map<String, Object> parameters);
    default ToolDefinition toDefinition() {
        return new ToolDefinition(getName(), getDescription(), getParametersSchema());
    }
}