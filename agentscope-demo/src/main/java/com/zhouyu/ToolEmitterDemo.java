package com.zhouyu;

import com.zhouyu.tools.WeatherService;
import io.agentscope.core.ReActAgent;
import io.agentscope.core.message.Msg;
import io.agentscope.core.model.DashScopeChatModel;
import io.agentscope.core.tool.Toolkit;

/**
 * 作者：IT周瑜
 * 公众号：IT周瑜
 * 微信号：it_zhouyu
 */
public class ToolEmitterDemo {

    public static void main(String[] args) {

        DashScopeChatModel chatModel = DashScopeChatModel.builder()
                .apiKey(System.getenv("DASHSCOPE_API_KEY"))
                .modelName("qwen3-max")
                .build();

        Toolkit toolkit = new Toolkit();
        toolkit.registerTool(new WeatherService());
        toolkit.setChunkCallback((toolUseBlock, toolResultBlock) -> {
            System.out.println(toolUseBlock.getName());
            System.out.println(toolUseBlock.getContent());
            System.out.println(toolResultBlock.getOutput());
        });

        ReActAgent agent = ReActAgent.builder()
                .name("Assistant")
                .sysPrompt("你是一个有帮助的 AI 助手。")
                .model(chatModel)
                .toolkit(toolkit)
                .build();

        Msg response = agent.call(Msg.builder()
                .textContent("生成10个数据")
                .build()).block();

        System.out.println(response.getTextContent());
    }
}
