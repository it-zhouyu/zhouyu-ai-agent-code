package com.zhouyu;

import com.zhouyu.tools.EmailService;
import io.agentscope.core.ReActAgent;
import io.agentscope.core.message.Msg;
import io.agentscope.core.model.DashScopeChatModel;
import io.agentscope.core.tool.Toolkit;

import java.util.Map;

/**
 * 作者：IT周瑜
 * 公众号：IT周瑜
 * 微信号：it_zhouyu
 */
public class ToolPresetDemo {

    public static void main(String[] args) {

        DashScopeChatModel chatModel = DashScopeChatModel.builder()
                .apiKey(System.getenv("DASHSCOPE_API_KEY"))
                .modelName("qwen3-max")
                .build();

        Toolkit toolkit = new Toolkit();
        toolkit.registration()
                .tool(new EmailService())
                .presetParameters(Map.of("send", Map.of("apiKey", "123123")))  // send工具中的apiKey参数为预设参数，不会发送给LLM，会直接将预设值直接传给工具
                .apply();

        ReActAgent agent = ReActAgent.builder()
                .name("Assistant")
                .sysPrompt("你是一个有帮助的 AI 助手。")
                .model(chatModel)
                .toolkit(toolkit)
                .build();

        Msg response = agent.call(Msg.builder()
                .textContent("发邮件给zhouyu,主题为Java")
                .build()).block();

        System.out.println(response.getTextContent());
    }
}
