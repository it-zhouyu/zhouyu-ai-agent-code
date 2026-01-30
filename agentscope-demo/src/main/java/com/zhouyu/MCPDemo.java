package com.zhouyu;

import io.agentscope.core.ReActAgent;
import io.agentscope.core.message.Msg;
import io.agentscope.core.model.DashScopeChatModel;
import io.agentscope.core.tool.Toolkit;
import io.agentscope.core.tool.mcp.McpClientBuilder;
import io.agentscope.core.tool.mcp.McpClientWrapper;

/**
 * 作者：IT周瑜
 * 公众号：IT周瑜
 * 微信号：it_zhouyu
 */
public class MCPDemo {

    public static void main(String[] args) {

        DashScopeChatModel chatModel = DashScopeChatModel.builder()
                .apiKey(System.getenv("DASHSCOPE_API_KEY"))
                .modelName("qwen3-max")
                .build();

        McpClientWrapper mcpClient = McpClientBuilder.create("remote-mcp")
                .streamableHttpTransport("http://localhost:8082/mcp")
                .buildAsync()
                .block();

        Toolkit toolkit = new Toolkit();
        toolkit.registerMcpClient(mcpClient).block();

        ReActAgent agent = ReActAgent.builder()
                .name("Assistant")
                .sysPrompt("你是一个有帮助的 AI 助手。")
                .model(chatModel)
                .toolkit(toolkit)
                .build();

        Msg response = agent.call(Msg.builder()
                .textContent("上海什么天气")
                .build()).block();

        System.out.println("TextContent: " + response.getTextContent());
        System.out.println("GenerateReason: " + response.getGenerateReason());

    }
}
