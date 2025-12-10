package com.zhouyu.server;

import com.alibaba.cloud.ai.graph.agent.ReactAgent;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.support.ToolCallbacks;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

/**
 * 作者：IT周瑜
 * 公众号：IT周瑜
 * 微信号：it_zhouyu
 */
@SpringBootApplication
public class A2AServerApplication {

    @Bean
    @Primary
    public ReactAgent weatherAgent(ChatModel chatModel) {
        ReactAgent weatherAgent = ReactAgent.builder()
                .name("weatherAgent")
                .model(chatModel)
                .systemPrompt("简短的回答用户问题")
                .tools(ToolCallbacks.from(new ZhouyuTools()))
                .outputKey("weatherResult")
                .build();
        return weatherAgent;
    }

    @Bean
    public ReactAgent weatherAgent1(ChatModel chatModel) {
        ReactAgent weatherAgent1 = ReactAgent.builder()
                .name("weatherAgent1")
                .model(chatModel)
                .systemPrompt("简短的回答用户问题")
                .tools(ToolCallbacks.from(new ZhouyuTools()))
                .outputKey("weatherResult")
                .build();
        return weatherAgent1;
    }

    public static void main(String[] args) {
        SpringApplication.run(A2AServerApplication.class, args);
    }
}
