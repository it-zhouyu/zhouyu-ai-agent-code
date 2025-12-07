package com.zhouyu;

import com.alibaba.cloud.ai.graph.NodeOutput;
import com.alibaba.cloud.ai.graph.RunnableConfig;
import com.alibaba.cloud.ai.graph.agent.ReactAgent;
import com.alibaba.cloud.ai.graph.checkpoint.Checkpoint;
import com.alibaba.cloud.ai.graph.checkpoint.savers.MemorySaver;
import com.alibaba.cloud.ai.graph.exception.GraphRunnerException;
import com.alibaba.cloud.ai.graph.streaming.StreamingOutput;
import com.zhouyu.tools.ZhouyuTools;
import lombok.extern.log4j.Log4j2;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

import java.util.Collection;
import java.util.Map;

/**
 * 作者：IT周瑜
 * 公众号：IT周瑜
 * 微信号：it_zhouyu
 */
@RestController
@Log4j2
public class AgentController {

    @Autowired
    private ChatClient.Builder chatClientBuilder;

    @Autowired
    private ReactAgent helloAgent;

    @Autowired
    private ReactAgent memoryAgent;

    @Autowired
    private ReactAgent hookAgent;

    @GetMapping("/hello")
    public String hello(String input) {
        try {
            AssistantMessage message = helloAgent.call(input);
            return message.getText();
        } catch (GraphRunnerException e) {
            throw new RuntimeException(e);
        }
    }

    @GetMapping(value = "/stream", produces = "text/html;charset=UTF-8")
    public Flux<String> stream(String input) {
        try {
            Flux<NodeOutput> outputFlux = helloAgent.stream(input);

            return outputFlux.map(output -> {
                log.info("output: {}", output);
                if (output instanceof StreamingOutput<?> streamingOutput) {
                    if (streamingOutput.message() != null) {
                        return streamingOutput.message().getText();
                    }
                }
                return "";
            });
        } catch (GraphRunnerException e) {
            throw new RuntimeException(e);
        }
    }


    @Autowired
    private MemorySaver memorySaver;

    @GetMapping("/memory")
    public String memory(String input, String chatId) {
        try {
            RunnableConfig runnableConfig = RunnableConfig.builder().threadId(chatId).build();
            AssistantMessage message = memoryAgent.call(input, runnableConfig);

            Collection<Checkpoint> list = memorySaver.list(runnableConfig);
            for (Checkpoint checkpoint : list) {
                log.info("checkpoint: {}", checkpoint);
            }

            return message.getText();
        } catch (GraphRunnerException e) {
            throw new RuntimeException(e);
        }
    }


    @GetMapping("/toolContext")
    public String toolContext(String input) {
        ChatClient chatClient = chatClientBuilder.build();

        return chatClient.prompt()
                .user(input)
                .tools(new ZhouyuTools())
                .toolContext(Map.of("input", input))
                .call()
                .content();
    }

    @GetMapping("/hook")
    public String hook(String input) {
        try {
            AssistantMessage message = hookAgent.call(input);
            return message.getText();
        } catch (GraphRunnerException e) {
            throw new RuntimeException(e);
        }
    }

}
