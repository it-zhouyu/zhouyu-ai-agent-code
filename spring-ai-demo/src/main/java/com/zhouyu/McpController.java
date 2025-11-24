package com.zhouyu;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 作者：IT周瑜
 * 公众号：IT周瑜
 * 微信号：it_zhouyu
 */
@RestController
public class McpController {

    @Autowired
    private ChatClient chatClient;

    @Autowired
    private ToolCallbackProvider toolCallbackProvider;

    // tool/list--->工具list
    // toolcall
    // tool/call

    @GetMapping("/mcp")
    public String mcp(String message) {
        return chatClient
                .prompt()
                .user(message)
                .toolCallbacks(toolCallbackProvider.getToolCallbacks())
                .call()
                .content();
    }
}
