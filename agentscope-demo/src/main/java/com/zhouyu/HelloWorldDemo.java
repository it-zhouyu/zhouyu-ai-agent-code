package com.zhouyu;

import com.zhouyu.tools.WeatherService;
import io.agentscope.core.ReActAgent;
import io.agentscope.core.message.Msg;
import io.agentscope.core.model.DashScopeChatModel;
import io.agentscope.core.tool.Toolkit;
import reactor.core.publisher.Mono;

/**
 * 作者：IT周瑜
 * 公众号：IT周瑜
 * 微信号：it_zhouyu
 */
public class HelloWorldDemo {

    public static void main(String[] args) {

        DashScopeChatModel chatModel = DashScopeChatModel.builder()
                .apiKey(System.getenv("DASHSCOPE_API_KEY"))
                .modelName("qwen3-max")
                .build();

        ReActAgent agent = ReActAgent.builder()
                .name("Assistant")
                .sysPrompt("你是一个有帮助的 AI 助手。")
                .model(chatModel)
                .build();

        Msg userMsg = Msg.builder().textContent("你好！").build();
        Mono<Msg> mono = agent.call(userMsg);
        Msg response = mono.block();
        System.out.println(response.getTextContent());
    }
}
