package com.zhouyu.node;

import com.alibaba.cloud.ai.graph.OverAllState;
import com.alibaba.cloud.ai.graph.action.NodeAction;
import com.alibaba.cloud.ai.graph.streaming.GraphFlux;
import com.alibaba.cloud.ai.toolcalling.tavily.TavilySearchConstants;
import com.alibaba.fastjson2.JSONObject;
import com.zhouyu.dto.Plan;
import com.zhouyu.dto.Step;
import com.zhouyu.util.PromptUtil;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.Generation;
import reactor.core.publisher.Flux;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

/**
 * 作者：IT周瑜
 * 公众号：IT周瑜
 * 微信号：it_zhouyu
 */
public class ResearcherNode implements NodeAction {

    private ChatClient chatClient;
    private Integer stepNum;
    private List<String> nodeResult = new ArrayList<>();

    public ResearcherNode(ChatClient chatClient, Integer stepNum) {
        this.chatClient = chatClient;
        this.stepNum = stepNum;
    }

    @Override
    public Map<String, Object> apply(OverAllState state) throws Exception {

        AssistantMessage assistantMessage = state.value("plannerResult", AssistantMessage.class).orElseThrow();
        String text = assistantMessage.getText();
        Plan plan = JSONObject.parseObject(text, Plan.class);

        String systemPrompt = PromptUtil.getPrompt("researcher");

        List<Step> steps = plan.getSteps();
        Step step = steps.get(stepNum);

        Flux<ChatResponse> chatResponseFlux = chatClient.prompt()
                .system(systemPrompt)
                .user(step.getPrompt())
                .toolNames(TavilySearchConstants.TOOL_NAME)
                .stream()
                .chatResponse();
        Flux<String> stringFlux = chatResponseFlux.map(chatResponse -> {
            Generation generation = chatResponse.getResult();
            if (generation == null) {
                return "";
            } else {
                return generation.getOutput().getText();
            }
        });

        Function<String, String> mapResult = lastChunk -> {
            nodeResult.add(lastChunk);
            return String.join("", nodeResult);
        };

        // 定义块结果提取函数
        Function<String, String> chunkResult = chunk -> chunk;

        GraphFlux<String> graphFlux = GraphFlux.of(
                "parallel_researcher_node_" + stepNum, // 节点 ID
                "researcherResult_" + stepNum, // 输出键
                stringFlux, // 流式数据
                mapResult, // 最终结果映射
                chunkResult // 块结果提取
        );

        return Map.of("researcherResultStream_" + stepNum, graphFlux);
    }
}
