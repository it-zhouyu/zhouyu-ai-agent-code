package com.zhouyu.client;

import com.alibaba.cloud.ai.graph.agent.ReactAgent;
import com.alibaba.cloud.ai.graph.agent.a2a.A2aRemoteAgent;
import com.alibaba.cloud.ai.graph.agent.a2a.AgentCardProvider;
import com.alibaba.cloud.ai.graph.agent.flow.agent.SequentialAgent;
import com.alibaba.cloud.ai.graph.exception.GraphStateException;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import java.util.List;

/**
 * 作者：IT周瑜
 * 公众号：IT周瑜
 * 微信号：it_zhouyu
 */
@Configuration
public class AgentConfig {

    @Autowired
    private AgentCardProvider agentCardProvider;

    @Bean
    @Primary
    public A2aRemoteAgent a2aRemoteAgent() {
        A2aRemoteAgent weatherAgent = A2aRemoteAgent.builder()
                .name("weatherAgent")
                .agentCardProvider(agentCardProvider)
                .description("专门用于获取天气的远程智能体")
                .instruction("用户输入：{input}")
                .build();

        return weatherAgent;
    }

    @Bean
    public SequentialAgent sequentialAgent(ChatModel chatModel) throws GraphStateException {
        A2aRemoteAgent weatherAgent = A2aRemoteAgent.builder()
                .name("weatherAgent")
                .agentCardProvider(agentCardProvider)
                .description("专门用于获取天气的远程智能体")
                .instruction("用户输入：{input}")
                .build();

        ReactAgent reactAgent = ReactAgent.builder()
                .name("reactAgent")
                .model(chatModel)
                .systemPrompt("写诗")
                .build();

        SequentialAgent sequentialAgent = SequentialAgent.builder()
                .name("sequentialAgent")
                .subAgents(List.of(weatherAgent, reactAgent))
                .build();

        return sequentialAgent;
    }

}
