package com.zhouyu;

import com.alibaba.cloud.ai.graph.CompiledGraph;
import com.alibaba.cloud.ai.graph.NodeOutput;
import com.alibaba.cloud.ai.graph.OverAllState;
import com.alibaba.cloud.ai.graph.RunnableConfig;
import com.alibaba.cloud.ai.graph.action.InterruptionMetadata;
import com.alibaba.cloud.ai.graph.streaming.StreamingOutput;
import com.alibaba.fastjson2.JSON;
import com.zhouyu.dto.Plan;
import lombok.extern.log4j.Log4j2;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

import java.util.Map;

/**
 * 作者：IT周瑜
 * 公众号：IT周瑜
 * 微信号：it_zhouyu
 */
@RestController
@Log4j2
public class DataController {

    @Autowired
    private CompiledGraph compiledGraph;

    @GetMapping(value = "/stream", produces = "text/html;charset=UTF-8")
    public Flux<String> stream(String input) {

        RunnableConfig config = RunnableConfig.builder().threadId("123").build();

        Flux<NodeOutput> outputFlux = compiledGraph.stream(Map.of("input", input), config);

        return outputFlux.map(output -> {
            if (output instanceof StreamingOutput<?> streamingOutput) {
                log.info("output: {}", output);
                if (streamingOutput.message() != null && streamingOutput.node().equals("reportGeneratorNode")) {
                    return streamingOutput.message().getText();
                }
            } else if (output instanceof InterruptionMetadata interruptionMetadata) {
                Plan plan = output.state().value("plannerResult", Plan.class).orElseThrow();
                return "计划生成完成，请确认! " + JSON.toJSONString(plan);
            }
            return "";
        });
    }

    @Autowired
    private ChatClient.Builder chatClientBuilder;

    @GetMapping(value = "/streamContinue", produces = "text/html;charset=UTF-8")
    public Flux<String> streamContinue(String input) throws Exception {
        RunnableConfig config = RunnableConfig.builder().threadId("123").build();

        Plan plan = compiledGraph.getState(config).state().value("plannerResult", Plan.class).orElseThrow();

        ChatClient chatClient = chatClientBuilder.build();
        Plan newPlan = chatClient
                .prompt()
                .system("按用户要求修改计划，以下是当前计划：" + JSON.toJSONString(plan))
                .user("用户要求：" + input)
                .call()
                .entity(Plan.class);

        RunnableConfig updatedConfig = compiledGraph.updateState(config, Map.of("plannerResult", newPlan));

        RunnableConfig resumeConfig = RunnableConfig.builder(updatedConfig)
                .addMetadata(RunnableConfig.HUMAN_FEEDBACK_METADATA_KEY, "placeholder")
                .build();

        Flux<NodeOutput> outputFlux = compiledGraph.streamFromInitialNode(compiledGraph.getState(updatedConfig).state(), resumeConfig);

        return outputFlux.map(output -> {
            if (output instanceof StreamingOutput<?> streamingOutput) {
                log.info("output: {}", output);
                if (streamingOutput.message() != null && streamingOutput.node().equals("reportGeneratorNode")) {
                    return streamingOutput.message().getText();
                }
            }
            return "";
        });
    }
}
