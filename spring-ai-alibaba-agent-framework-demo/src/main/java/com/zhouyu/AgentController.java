package com.zhouyu;

import com.alibaba.cloud.ai.graph.NodeOutput;
import com.alibaba.cloud.ai.graph.OverAllState;
import com.alibaba.cloud.ai.graph.RunnableConfig;
import com.alibaba.cloud.ai.graph.action.InterruptionMetadata;
import com.alibaba.cloud.ai.graph.agent.ReactAgent;
import com.alibaba.cloud.ai.graph.agent.flow.agent.LlmRoutingAgent;
import com.alibaba.cloud.ai.graph.agent.flow.agent.ParallelAgent;
import com.alibaba.cloud.ai.graph.agent.flow.agent.SequentialAgent;
import com.alibaba.cloud.ai.graph.checkpoint.Checkpoint;
import com.alibaba.cloud.ai.graph.checkpoint.savers.MemorySaver;
import com.alibaba.cloud.ai.graph.exception.GraphRunnerException;
import com.alibaba.cloud.ai.graph.store.stores.MemoryStore;
import com.alibaba.cloud.ai.graph.streaming.StreamingOutput;
import com.zhouyu.agent.ZhouyuAgent;
import com.zhouyu.tools.ZhouyuTools;
import lombok.extern.log4j.Log4j2;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

import java.util.*;

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


    @Autowired
    private ReactAgent humanHookAgent;


    private Map<String, InterruptionMetadata> interruptionMetadataSessions = new HashMap<>();

    @GetMapping("/humanHook")
    public String humanHook(String input, String threadId) {

        RunnableConfig runnableConfig = RunnableConfig.builder().threadId(threadId).build();
        try {
            Optional<NodeOutput> result = humanHookAgent.invokeAndGetOutput(input, runnableConfig);

            if (result.isPresent() && result.get() instanceof InterruptionMetadata interruptionMetadata) {
                List<InterruptionMetadata.ToolFeedback> toolFeedbacks = interruptionMetadata.toolFeedbacks();
                for (InterruptionMetadata.ToolFeedback feedback : toolFeedbacks) {
                    System.out.println("工具: " + feedback.getName());
                    System.out.println("参数: " + feedback.getArguments());
                    System.out.println("描述: " + feedback.getDescription());
                }
                interruptionMetadataSessions.put(threadId, interruptionMetadata);

                return toolFeedbacks.get(0).getDescription();
            }

            return result.orElseThrow().state().toString();
        } catch (GraphRunnerException e) {
            throw new RuntimeException(e);
        }


    }

    @GetMapping("/humanAgentFeedback")
    public String humanAgentFeedback(String threadId) throws GraphRunnerException {
        InterruptionMetadata interruptionMetadata = interruptionMetadataSessions.get(threadId);

        InterruptionMetadata.Builder feedbackBuilder = InterruptionMetadata.builder()
                .nodeId(interruptionMetadata.node())
                .state(interruptionMetadata.state());

        List<InterruptionMetadata.ToolFeedback> toolFeedbacks = interruptionMetadata.toolFeedbacks();
        toolFeedbacks.forEach(toolFeedback -> {
            InterruptionMetadata.ToolFeedback approvedFeedback =
                    InterruptionMetadata.ToolFeedback.builder(toolFeedback)
                            .result(InterruptionMetadata.ToolFeedback.FeedbackResult.APPROVED)
                            .build();
            feedbackBuilder.addToolFeedback(approvedFeedback);
        });

        InterruptionMetadata approvalMetadata = feedbackBuilder.build();
        RunnableConfig resumeConfig = RunnableConfig.builder()
                .threadId(threadId)
                .addMetadata(RunnableConfig.HUMAN_FEEDBACK_METADATA_KEY, approvalMetadata)
                .build();

        Optional<NodeOutput> finalResult = humanHookAgent.invokeAndGetOutput("", resumeConfig);

        if (finalResult.isPresent()) {
            System.out.println("执行完成");
            System.out.println("最终结果: " + finalResult.get());
        }

        return finalResult.orElseThrow().state().toString();
    }

    @Autowired
    private MemoryStore memoryStore;

    @Autowired
    private ReactAgent storeAgent;

    @GetMapping("/store")
    public String store(String input, String threadId) {
        try {
            RunnableConfig config = RunnableConfig.builder().threadId(threadId).store(memoryStore).build();
            AssistantMessage assistantMessage = storeAgent.call(input, config);
            return assistantMessage.getText();
        } catch (GraphRunnerException e) {
            throw new RuntimeException(e);
        }
    }


    @Autowired
    private ReactAgent defaultHookAgent;

    @GetMapping("/defaultHook")
    public String defaultHook(String input, String threadId) {
        try {
            RunnableConfig config = RunnableConfig.builder().threadId(threadId).build();
            AssistantMessage assistantMessage = defaultHookAgent.call(input, config);
            return assistantMessage.getText();
        } catch (GraphRunnerException e) {
            throw new RuntimeException(e);
        }
    }

    @Autowired
    private SequentialAgent sequentialAgent;

    @Autowired
    private ParallelAgent parallelAgent;

    @Autowired
    private LlmRoutingAgent llmRoutingAgent;

    @Autowired
    private ZhouyuAgent zhouyuAgent;

    @Autowired
    private SequentialAgent complexWorkflow;

    @Autowired
    private ReactAgent toolAgent;

    @Autowired
    private ReactAgent ragAgent;

    @GetMapping("/multiAgent")
    public Map<String, Object> multiAgent(String input) {
        try {
//            Optional<OverAllState> overAllState = sequentialAgent.invoke(input);
//            Optional<OverAllState> overAllState = parallelAgent.invoke(input);
//            Optional<OverAllState> overAllState = llmRoutingAgent.invoke(input);
//            Optional<OverAllState> overAllState = zhouyuAgent.invoke(input);
//            Optional<OverAllState> overAllState = complexWorkflow.invoke(input);
            Optional<OverAllState> overAllState = ragAgent.invoke(input);
            return overAllState.orElseThrow().data();
        } catch (GraphRunnerException e) {
            throw new RuntimeException();
        }
    }

}
