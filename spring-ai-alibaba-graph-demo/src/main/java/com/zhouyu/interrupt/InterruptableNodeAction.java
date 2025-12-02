package com.zhouyu.interrupt;

/**
 * 作者：IT周瑜
 * 公众号：IT周瑜
 * 微信号：it_zhouyu
 */

import com.alibaba.cloud.ai.graph.OverAllState;
import com.alibaba.cloud.ai.graph.RunnableConfig;
import com.alibaba.cloud.ai.graph.action.*;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatResponse;
import reactor.core.publisher.Flux;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

/**
 * 作者：IT周瑜
 * 公众号：IT周瑜
 * 微信号：it_zhouyu
 */
public class InterruptableNodeAction implements AsyncNodeActionWithConfig, InterruptableAction  {

    private ChatClient chatClient;

    public InterruptableNodeAction(ChatClient chatClient) {
        this.chatClient = chatClient;
    }

    @Override
    public CompletableFuture<Map<String, Object>> apply(OverAllState state, RunnableConfig config) {
        Flux<ChatResponse> chatResponseFlux = chatClient.prompt("直接返回“我是节点2”")
                .stream()
                .chatResponse();
        return CompletableFuture.completedFuture(Map.of("node2Result", chatResponseFlux));
    }

    @Override
    public Optional<InterruptionMetadata> interrupt(String nodeId, OverAllState state, RunnableConfig config) {
        // 如果状态中没有 humanFeedback，则中断等待用户输入
        Optional<Object> humanFeedback = state.value("humanFeedbackResult");

        if (humanFeedback.isEmpty()) {
            // 返回 InterruptionMetadata 来中断执行
            InterruptionMetadata interruption = InterruptionMetadata.builder(nodeId, state)
                    .addMetadata("message", "等待用户输入...")
                    .addMetadata("node", nodeId)
                    .build();

            return Optional.of(interruption);
        }

        // 如果已经有humanFeedback，继续执行
        return Optional.empty();
    }
}
