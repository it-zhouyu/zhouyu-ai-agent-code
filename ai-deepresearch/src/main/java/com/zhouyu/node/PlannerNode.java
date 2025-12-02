package com.zhouyu.node;

import com.alibaba.cloud.ai.graph.OverAllState;
import com.alibaba.cloud.ai.graph.action.NodeAction;
import com.alibaba.cloud.ai.toolcalling.tavily.TavilySearchConstants;
import com.zhouyu.util.PromptUtil;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatResponse;
import reactor.core.publisher.Flux;

import java.util.Map;

/**
 * 作者：IT周瑜
 * 公众号：IT周瑜
 * 微信号：it_zhouyu
 */
public class PlannerNode implements NodeAction {

    private ChatClient chatClient;

    public PlannerNode(ChatClient chatClient) {
        this.chatClient = chatClient;
    }

    @Override
    public Map<String, Object> apply(OverAllState state) throws Exception {

        String input = state.value("input", String.class).orElseThrow();
        String systemPrompt = PromptUtil.getPrompt("planner");

        Flux<ChatResponse> chatResponseFlux = chatClient.prompt()
                .system(systemPrompt)
                .user(input)
                .toolNames(TavilySearchConstants.TOOL_NAME)
                .stream()
                .chatResponse();

        return Map.of("plannerResult", chatResponseFlux);
    }
}
