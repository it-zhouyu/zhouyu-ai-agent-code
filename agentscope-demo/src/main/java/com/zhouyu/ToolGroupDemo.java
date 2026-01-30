package com.zhouyu;

import com.zhouyu.tools.WeatherService;
import io.agentscope.core.ReActAgent;
import io.agentscope.core.message.Msg;
import io.agentscope.core.model.DashScopeChatModel;
import io.agentscope.core.tool.Toolkit;

import java.util.List;

/**
 * 作者：IT周瑜
 * 公众号：IT周瑜
 * 微信号：it_zhouyu
 */
public class ToolGroupDemo {

    public static void main(String[] args) {

        DashScopeChatModel chatModel = DashScopeChatModel.builder()
                .apiKey(System.getenv("DASHSCOPE_API_KEY"))
                .modelName("qwen3-max")
                .build();

        Toolkit toolkit = new Toolkit();

        // 创建工具组，默认都启用
        toolkit.createToolGroup("admin", "管理工具");
        toolkit.createToolGroup("basic", "基础工具");

        // 给basic组注册工具
        toolkit.registration()
                .tool(new WeatherService())
                .group("basic")
                .apply();

        ReActAgent agent1 = ReActAgent.builder()
                .name("Assistant")
                .sysPrompt("你是一个有帮助的 AI 助手。")
                .model(chatModel)
                .toolkit(toolkit)
                .build();

        Msg response1 = agent1.call(Msg.builder()
                .textContent("上海什么天气")
                .build()).block();

        System.out.println(response1.getTextContent());

        // 动态切换，关闭basic
        toolkit.updateToolGroups(List.of("basic"), false);

        ReActAgent agent2 = ReActAgent.builder()
                .name("Assistant")
                .sysPrompt("你是一个有帮助的 AI 助手。")
                .model(chatModel)
                .toolkit(toolkit)
                .build();

        Msg response2 = agent2.call(Msg.builder()
                .textContent("上海什么天气")
                .build()).block();

        System.out.println(response2.getTextContent());
    }
}
