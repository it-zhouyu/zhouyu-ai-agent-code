package com.zhouyu.node;

import com.alibaba.cloud.ai.graph.OverAllState;
import com.alibaba.cloud.ai.graph.action.NodeAction;
import com.alibaba.fastjson2.JSON;
import com.zhouyu.dto.Plan;
import com.zhouyu.dto.StepResultDto;
import com.zhouyu.util.PromptUtil;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatResponse;
import reactor.core.publisher.Flux;

import java.util.HashMap;
import java.util.Map;

/**
 * 作者：IT周瑜
 * 公众号：IT周瑜
 * 微信号：it_zhouyu
 */
public class ReportGeneratorNode implements NodeAction {
    private ChatClient chatClient;

    public ReportGeneratorNode(ChatClient chatClient) {
        this.chatClient = chatClient;
    }

    @Override
    public Map<String, Object> apply(OverAllState state) throws Exception {
        HashMap<String, StepResultDto> planExecuteResult = state.value("planExecuteResult", new HashMap<String, StepResultDto>());
        String userInput = state.value("input", String.class).orElseThrow();
        Plan plan = state.value("plannerResult", Plan.class).orElseThrow();

        String prompt = PromptUtil.getPrompt("report");
        prompt = prompt.replace("{{user_input}}", userInput);
        prompt = prompt.replace("{{plan}}", JSON.toJSONString(plan));
        prompt = prompt.replace("{{stepResult}}", JSON.toJSONString(planExecuteResult));

        Flux<ChatResponse> content = chatClient.prompt()
                .user(prompt)
                .stream()
                .chatResponse();

        return Map.of("reportGeneratorResult", content);
    }
}
