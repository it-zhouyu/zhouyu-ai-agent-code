package com.zhouyu;

import com.alibaba.cloud.ai.graph.*;
import com.alibaba.cloud.ai.graph.exception.GraphStateException;
import com.alibaba.cloud.ai.graph.observation.GraphObservationLifecycleListener;
import com.alibaba.cloud.ai.graph.streaming.StreamingOutput;
import io.micrometer.observation.ObservationRegistry;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import reactor.core.publisher.Flux;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@RestController
@Log4j2
public class DeepResearchController {

    @Autowired
    private CompiledGraph compiledGraph;

    @GetMapping(value = "/stream", produces = "text/html;charset=UTF-8")
    public Flux<String> stream(String input) {
        Flux<NodeOutput> outputFlux = compiledGraph.stream(Map.of("input", input));

        return outputFlux.map(output -> {
            log.info("output: {}", output);
            if (output instanceof StreamingOutput<?> streamingOutput) {
                if (streamingOutput.message() != null && !streamingOutput.node().equals("coordinatorNode")) {
                    return streamingOutput.message().getText();
                }
            }
            return "";
        });
    }

    @GetMapping(value = "/sse")
    public SseEmitter sse(String input) {
        SseEmitter sseEmitter = new SseEmitter(300000L) {
            @Override
            protected void extendResponse(ServerHttpResponse outputMessage) {
                HttpHeaders headers = outputMessage.getHeaders();
                headers.setContentType(new MediaType("text", "event-stream", StandardCharsets.UTF_8));
            }
        };

        RunnableConfig runnableConfig = RunnableConfig.builder().addParallelNodeExecutor("plannerNode", Executors.newFixedThreadPool(10)).build();
        Flux<String> outputFlux = compiledGraph.stream(Map.of("input", input), runnableConfig)
                .map(output -> {
                    log.info("output: {}", output);
                    if (output instanceof StreamingOutput<?> streamingOutput) {
                        if (streamingOutput.message() != null && !streamingOutput.node().equals("coordinatorNode")) {
                            return streamingOutput.message().getText();
                        } else if (streamingOutput.chunk() != null && !streamingOutput.node().equals("coordinatorNode")) {
                            return streamingOutput.chunk();
                        }
                    }
                    return "";
                });

        outputFlux.subscribe(token -> {
            try {
                sseEmitter.send(Map.of("content", token));
            } catch (IOException e) {
                sseEmitter.completeWithError(e);
            }
        }, sseEmitter::completeWithError, sseEmitter::complete);

        return sseEmitter;
    }
}