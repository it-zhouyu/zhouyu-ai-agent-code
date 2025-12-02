package com.zhouyu.node;

import com.alibaba.cloud.ai.graph.OverAllState;
import com.alibaba.cloud.ai.graph.action.NodeAction;
import com.alibaba.cloud.ai.toolcalling.tavily.TavilySearchConstants;
import com.alibaba.fastjson2.JSONObject;
import com.zhouyu.dto.Plan;
import com.zhouyu.dto.Step;
import com.zhouyu.util.PromptUtil;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.model.ChatResponse;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.Map;

/**
 * 作者：IT周瑜
 * 公众号：IT周瑜
 * 微信号：it_zhouyu
 */
public class ResearcherNode implements NodeAction {

    private ChatClient chatClient;

    public ResearcherNode(ChatClient chatClient) {
        this.chatClient = chatClient;
    }

    @Override
    public Map<String, Object> apply(OverAllState state) throws Exception {

        AssistantMessage assistantMessage = state.value("plannerResult", AssistantMessage.class).orElseThrow();
        String text = assistantMessage.getText();
        Plan plan = JSONObject.parseObject(text, Plan.class);

        String systemPrompt = PromptUtil.getPrompt("researcher");


        Flux<ChatResponse> mergedFlux = Flux.empty();
        List<Step> steps = plan.getSteps();
        for (Step step : steps) {
            Flux<ChatResponse> chatResponseFlux = chatClient.prompt()
                    .system(systemPrompt)
                    .user(step.getPrompt())
                    .toolNames(TavilySearchConstants.TOOL_NAME)
                    .stream()
                    .chatResponse();
            mergedFlux = mergedFlux.concatWith(chatResponseFlux);
        }

        return Map.of("researcherResult", mergedFlux);
    }
}
