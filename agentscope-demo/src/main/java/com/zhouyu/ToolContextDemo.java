package com.zhouyu;

import com.zhouyu.context.UserContext;
import com.zhouyu.tools.EmailService;
import com.zhouyu.tools.WeatherService;
import io.agentscope.core.ReActAgent;
import io.agentscope.core.message.Msg;
import io.agentscope.core.model.DashScopeChatModel;
import io.agentscope.core.tool.ToolExecutionContext;
import io.agentscope.core.tool.Toolkit;

import java.util.Map;

/**
 * 作者：IT周瑜
 * 公众号：IT周瑜
 * 微信号：it_zhouyu
 */
public class ToolContextDemo {

    public static void main(String[] args) {

        DashScopeChatModel chatModel = DashScopeChatModel.builder()
                .apiKey(System.getenv("DASHSCOPE_API_KEY"))
                .modelName("qwen3-max")
                .build();

        Toolkit toolkit = new Toolkit();
        toolkit.registration()
                .tool(new WeatherService())
                .apply();

        ToolExecutionContext context = ToolExecutionContext.builder()
                .register(new UserContext("user-123"))
                .build();

        ReActAgent agent = ReActAgent.builder()
                .name("Assistant")
                .sysPrompt("你是一个有帮助的 AI 助手。")
                .model(chatModel)
                .toolkit(toolkit)
                .toolExecutionContext(context)
                .build();

        // 相当于会自动带上ToolExecutionContext中的用户数据
        Msg response = agent.call(Msg.builder()
                .textContent("查询最近一个月的订单数据")
                .build()).block();

        System.out.println(response.getTextContent());
    }
}
