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
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatResponse;
import reactor.core.publisher.Flux;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 作者：IT周瑜
 * 公众号：IT周瑜
 * 微信号：it_zhouyu
 */
public class ReporterNode implements NodeAction {

    private ChatClient chatClient;

    public ReporterNode(ChatClient chatClient) {
        this.chatClient = chatClient;
    }

    @Override
    public Map<String, Object> apply(OverAllState state) throws Exception {

        AssistantMessage plannerResultAssistantMessage = state.value("plannerResult", AssistantMessage.class).orElseThrow();
        String plannerResultText = plannerResultAssistantMessage.getText();
        Plan plan = JSONObject.parseObject(plannerResultText, Plan.class);
        List<Step> steps = plan.getSteps();

        List<Message> messages = new ArrayList<>();
        for (int i = 0; i < steps.size(); i++) {
            String researcherResultText = state.value("researcherResult_" + i, String.class).orElseThrow();
            messages.add(new UserMessage(researcherResultText));
        }

        String systemPrompt = PromptUtil.getPrompt("reporter");

        Flux<ChatResponse> chatResponseFlux = chatClient.prompt()
                .system(systemPrompt)
                .messages(messages)
                .toolNames(TavilySearchConstants.TOOL_NAME)
                .stream()
                .chatResponse();

        return Map.of("reporterResult", chatResponseFlux);
    }
}
