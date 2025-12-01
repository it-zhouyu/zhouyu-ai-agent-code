package com.zhouyu;

import com.alibaba.cloud.ai.graph.CompiledGraph;
import com.alibaba.cloud.ai.graph.NodeOutput;
import com.alibaba.cloud.ai.graph.OverAllState;
import com.alibaba.cloud.ai.graph.RunnableConfig;
import com.alibaba.cloud.ai.graph.checkpoint.Checkpoint;
import com.alibaba.cloud.ai.graph.checkpoint.savers.MemorySaver;
import com.alibaba.cloud.ai.graph.exception.GraphStateException;
import com.alibaba.cloud.ai.graph.streaming.StreamingOutput;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;

/**
 * 作者：IT周瑜
 * 公众号：IT周瑜
 * 微信号：it_zhouyu
 */
@RestController
@Log4j2
public class GraphController {

    @Autowired
    private CompiledGraph simpleStateGraph;

    @Autowired
    private CompiledGraph conditionalStateGraph;

    @Autowired
    private CompiledGraph helloStateGraph;

    @GetMapping("/simple")
    public Map<String, Object> simple(String subject) {
        Optional<OverAllState> overAllState = simpleStateGraph.invoke(Map.of("subject", subject));
        OverAllState state = overAllState.orElseThrow();
        return state.data();
    }

    @GetMapping(value = "/stream", produces = "text/html;charset=UTF-8")
    public Flux<String> stream(String subject) {

        // 数据流
        Flux<NodeOutput> outputFlux = simpleStateGraph.stream(Map.of("subject", subject));

        return outputFlux.map(output -> {
            log.info("output: {}", output);
            if (output instanceof StreamingOutput<?> streamingOutput) {
                if (streamingOutput.message() != null && streamingOutput.node().equals("content")) {
                    return streamingOutput.message().getText();
                }
            }
            return "";
        });
    }

    @GetMapping("/conditional")
    public Map<String, Object> conditional(String input) {
        Optional<OverAllState> overAllState = conditionalStateGraph.invoke(Map.of("input", input));
        OverAllState state = overAllState.orElseThrow();
        return state.data();
    }

    @GetMapping(value = "/thread")
    public String thread(@RequestParam String chatId, @RequestParam String input) throws GraphStateException {
        RunnableConfig config = RunnableConfig.builder().threadId(chatId).build();

        Optional<OverAllState> overAllState = helloStateGraph.invoke(Map.of("input", input, "chatId", chatId), config);
        return overAllState.orElseThrow().value("result", String.class).orElseThrow();
    }

    @Autowired
    private MemorySaver memorySaver;

    @GetMapping(value = "/memorySaver")
    public String memorySaver(@RequestParam String chatId, @RequestParam String input) throws GraphStateException {
        RunnableConfig config = RunnableConfig.builder().threadId(chatId).build();
        Optional<OverAllState> overAllState = helloStateGraph.invoke(Map.of("input", input, "chatId", chatId), config);

        Collection<Checkpoint> list = memorySaver.list(config);
        for (Checkpoint checkpoint : list) {
            log.info("checkpoint: {}", checkpoint);
        }

        return overAllState.orElseThrow().value("result", String.class).orElseThrow();
    }
}
