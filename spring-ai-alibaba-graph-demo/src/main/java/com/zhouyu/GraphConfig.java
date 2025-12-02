package com.zhouyu;

import com.alibaba.cloud.ai.graph.*;
import com.alibaba.cloud.ai.graph.action.AsyncCommandAction;
import com.alibaba.cloud.ai.graph.action.EdgeAction;
import com.alibaba.cloud.ai.graph.action.NodeAction;
import com.alibaba.cloud.ai.graph.checkpoint.Checkpoint;
import com.alibaba.cloud.ai.graph.checkpoint.config.SaverConfig;
import com.alibaba.cloud.ai.graph.checkpoint.savers.MemorySaver;
import com.alibaba.cloud.ai.graph.exception.GraphStateException;
import com.alibaba.cloud.ai.graph.store.StoreItem;
import com.alibaba.cloud.ai.graph.store.stores.MemoryStore;
import com.zhouyu.blog.ContentNodeAction;
import com.zhouyu.blog.TitleNodeAction;
import com.zhouyu.interrupt.InterruptableNodeAction;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.InMemoryChatMemoryRepository;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import reactor.core.publisher.Flux;

import java.util.*;

import static com.alibaba.cloud.ai.graph.StateGraph.END;
import static com.alibaba.cloud.ai.graph.StateGraph.START;
import static com.alibaba.cloud.ai.graph.action.AsyncEdgeAction.edge_async;
import static com.alibaba.cloud.ai.graph.action.AsyncNodeAction.node_async;

/**
 * 作者：IT周瑜
 * 公众号：IT周瑜
 * 微信号：it_zhouyu
 */
@Configuration
public class GraphConfig {

    @Bean
    public CompiledGraph simpleStateGraph(ChatClient.Builder chatClientBuilder) throws GraphStateException {
        ChatClient chatClient = chatClientBuilder.build();

        StateGraph stateGraph = new StateGraph();

        stateGraph.addNode("title", node_async(new TitleNodeAction(chatClient)))
                .addNode("content", node_async(new ContentNodeAction(chatClient)));

        stateGraph.addEdge(START, "title")
                .addEdge("title", "content")
                .addEdge("content", END);

        CompiledGraph compiledGraph = stateGraph.compile();

        GraphRepresentation representation = compiledGraph.getGraph(GraphRepresentation.Type.MERMAID);
        System.out.println(representation.content());

        return compiledGraph;
    }


    @Bean
    public CompiledGraph conditionalStateGraph(ChatClient.Builder chatClientBuilder) throws GraphStateException {

        ChatClient chatClient = chatClientBuilder.build();
        StateGraph stateGraph = new StateGraph();

        // 意图识别
        stateGraph.addNode("intention", node_async(new NodeAction() {
            @Override
            public Map<String, Object> apply(OverAllState state) throws Exception {

                String input = state.value("input", String.class).orElseThrow();

                String intentionResult = chatClient.prompt()
                        .system("判断用户输入是要写诗还是要写代码，如果是写诗就返回数字1，如果是写代码就返回数字2")
                        .user(input)
                        .call()
                        .content();
                return Map.of("intentionResult", intentionResult);
            }
        }));

        stateGraph.addNode("poem", node_async(new NodeAction() {
            @Override
            public Map<String, Object> apply(OverAllState state) throws Exception {

                String input = state.value("input", String.class).orElseThrow();
                String poemResult = chatClient.prompt()
                        .system("根据用户输入写七言绝句")
                        .user(input)
                        .call()
                        .content();
                return Map.of("result", poemResult);
            }
        }));

        stateGraph.addNode("code", node_async(new NodeAction() {
            @Override
            public Map<String, Object> apply(OverAllState state) throws Exception {

                String input = state.value("input", String.class).orElseThrow();
                String codeResult = chatClient.prompt()
                        .system("根据用户输入写Java代码")
                        .user(input)
                        .call()
                        .content();
                return Map.of("result", codeResult);
            }
        }));

        stateGraph.addEdge(START, "intention")
                .addConditionalEdges("intention", edge_async(new EdgeAction() {
                            @Override
                            public String apply(OverAllState state) throws Exception {
                                return state.value("intentionResult", String.class).orElseThrow();
                            }
                        })
                        , Map.of("1", "poem", "2", "code"))
                .addEdge("poem", END)
                .addEdge("code", END)
        ;

        CompiledGraph compiledGraph = stateGraph.compile();
        GraphRepresentation representation = compiledGraph.getGraph(GraphRepresentation.Type.MERMAID);
        System.out.println(representation.content());

        return compiledGraph;

    }

    @Bean
    public ChatMemory chatMemory() {
        InMemoryChatMemoryRepository chatMemoryRepository = new InMemoryChatMemoryRepository();
        return MessageWindowChatMemory.builder()
                .chatMemoryRepository(chatMemoryRepository)
                .build();
    }

    @Bean
    public MemorySaver memorySaver() {
        return new MemorySaver();
    }


    // mysql
    @Bean
    public MemoryStore memoryStore() {

        MemoryStore memoryStore = new MemoryStore();

        // 相当于mysql中的数据
        Map<String, Object> userData = new HashMap<>();
        userData.put("username", "周瑜");
        StoreItem storeItem = StoreItem.of(List.of("user_info"), "user_002", userData);
        memoryStore.putItem(storeItem);

        return memoryStore;
    }

    @Bean
    public CompiledGraph helloStateGraph(ChatClient.Builder chatClientBuilder, ChatMemory chatMemory, MemorySaver memorySaver, MemoryStore memoryStore) throws GraphStateException {

        ChatClient chatClient = chatClientBuilder
//                .defaultAdvisors(MessageChatMemoryAdvisor.builder(chatMemory).build())
                .build();

        StateGraph stateGraph = new StateGraph();

        stateGraph.addNode("hello", node_async(new NodeAction() {
            @Override
            public Map<String, Object> apply(OverAllState state) throws Exception {
//                Integer step = state.value("step", 0);
//                return Map.of("step", step+1);

                String input = state.value("input", String.class).orElseThrow();
                String chatId = state.value("chatId", String.class).orElseThrow();

//                RunnableConfig config = RunnableConfig.builder()
//                        .threadId(chatId)
//                        .build();
//                Collection<Checkpoint> list = memorySaver.list(config);
//                List<Message> messages = new ArrayList<>();
//                for (Checkpoint checkpoint : list) {
//                    Map<String, Object> state1 = checkpoint.getState();
//                    if (state1.containsKey("input")) {
//                        messages.add(UserMessage.builder().text(state1.get("input").toString()).build());
//                    } else if (state1.containsKey("result")) {
//                        messages.add(AssistantMessage.builder().content(state1.get("result").toString()).build());
//                    }
//                }

                Optional<StoreItem> storeItem = memoryStore.getItem(List.of("user_info"), "user_002");


                String content = chatClient.prompt()
//                        .advisors(advisorSpec -> advisorSpec.params(Map.of(ChatMemory.CONVERSATION_ID, chatId)))
//                        .messages(messages)
                        .messages(UserMessage.builder().text(storeItem.get().getValue().get("username").toString()).build())
                        .user(input)
                        .call()
                        .content();
                return Map.of("result", content);
            }
        }));

        stateGraph.addEdge(START, "hello")
                .addEdge("hello", END);


        // checkpoint
        SaverConfig saverConfig = SaverConfig.builder()
                .register(memorySaver)
                .build();
        CompileConfig compileConfig = CompileConfig.builder()
                .saverConfig(saverConfig)
                .build();
        CompiledGraph compiledGraph = stateGraph.compile(compileConfig);

        return compiledGraph;
    }


    @Bean
    public CompiledGraph interruptBeforeStateGraph(ChatClient.Builder chatClientBuilder, MemorySaver memorySaver) throws GraphStateException {

        ChatClient chatClient = chatClientBuilder.build();

        // 定义普通节点
        var node1 = node_async(state -> {
            Flux<ChatResponse> chatResponseFlux = chatClient.prompt("直接返回“我是节点1”")
                    .stream()
                    .chatResponse();
            return Map.of("node1Result", chatResponseFlux);
        });

        var node2 = node_async(state -> {
            Flux<ChatResponse> chatResponseFlux = chatClient.prompt("直接返回“我是节点2”")
                    .stream()
                    .chatResponse();
            return Map.of("node2Result", chatResponseFlux);
        });

        var node3 = node_async(state -> {
            Flux<ChatResponse> chatResponseFlux = chatClient.prompt("直接返回“我是节点3”")
                    .stream()
                    .chatResponse();
            return Map.of("node3Result", chatResponseFlux);
        });

        StateGraph stateGraph = new StateGraph();
        stateGraph
                .addNode("node1", node1)
                .addNode("node2", node2)
                .addNode("node3", node3);

        stateGraph.addEdge(START, "node1")
                .addEdge("node1", "node2")
                .addConditionalEdges("node2", edge_async(state -> {
                            var humanFeedbackResult = (String) state.value("humanFeedbackResult").orElse("unknown");
                            return humanFeedbackResult.equals("next") ? "next" : "unknown";
                        }),
                        Map.of("next", "node3", "unknown", "node2"))
                .addEdge("node3", END);

        var compileConfig = CompileConfig.builder()
                .saverConfig(SaverConfig.builder()
                        .register(memorySaver)
                        .build())
                .interruptBefore("node2")
                .build();

        CompiledGraph compiledGraph = stateGraph.compile(compileConfig);

        GraphRepresentation representation = compiledGraph.getGraph(GraphRepresentation.Type.MERMAID);
        System.out.println(representation.content());

        return compiledGraph;
    }

    @Bean
    public CompiledGraph interruptStateGraph(ChatClient.Builder chatClientBuilder, MemorySaver memorySaver) throws GraphStateException {

        ChatClient chatClient = chatClientBuilder.build();

        // 定义普通节点
        var node1 = node_async(state -> {
            Flux<ChatResponse> chatResponseFlux = chatClient.prompt("直接返回“我是节点1”")
                    .stream()
                    .chatResponse();
            return Map.of("node1Result", chatResponseFlux);
        });

        var node2 = new InterruptableNodeAction(chatClient);

        var node3 = node_async(state -> {
            Flux<ChatResponse> chatResponseFlux = chatClient.prompt("直接返回“我是节点3”")
                    .stream()
                    .chatResponse();
            return Map.of("node3Result", chatResponseFlux);
        });

        StateGraph stateGraph = new StateGraph();
        stateGraph
                .addNode("node1", node1)
                .addNode("node2", node2)
                .addNode("node3", node3);

        stateGraph.addEdge(START, "node1")
                .addEdge("node1", "node2")
                .addConditionalEdges("node2", edge_async(state -> {
                            var humanFeedbackResult = (String) state.value("humanFeedbackResult").orElse("unknown");
                            return humanFeedbackResult.equals("next") ? "next" : "unknown";
                        }),
                        Map.of("next", "node3", "unknown", "node2"))
                .addEdge("node3", END);

        var compileConfig = CompileConfig.builder()
                .saverConfig(SaverConfig.builder()
                        .register(memorySaver)
                        .build())
                .build();

        CompiledGraph compiledGraph = stateGraph.compile(compileConfig);

        GraphRepresentation representation = compiledGraph.getGraph(GraphRepresentation.Type.MERMAID);
        System.out.println(representation.content());

        return compiledGraph;
    }

    @Bean("parallelExecutorStateGraph")
    public CompiledGraph parallelExecutorStateGraph() throws GraphStateException {

        NodeAction a = state -> Map.of();

        NodeAction b = state -> {
            System.out.println(Thread.currentThread().getName());
            System.out.println("开始执行 b");
            Thread.sleep(2000);
            System.out.println("结束执行 b");
            return Map.of("b_complete", true);
        };

        NodeAction c = state -> {
            System.out.println(Thread.currentThread().getName());
            System.out.println("开始执行 c");
            Thread.sleep(5000);
            System.out.println("结束执行 c");
            return Map.of("c_complete", true);
        };

        NodeAction d = state -> {
            Boolean bComplete = state.value("b_complete", false);
            Boolean cComplete = state.value("c_complete", false);
            if (bComplete && cComplete) {
                return Map.of("next_node", END);
            }
            return Map.of("next_node", "a");
        };

        StateGraph stateGraph = new StateGraph()
                .addNode("a", node_async(a))
                .addNode("b", node_async(b))
                .addNode("c", node_async(c))
                .addNode("d", node_async(d))

                .addEdge(START, "a")
                .addEdge("a", "b").addEdge("b", "d")
                .addEdge("a", "c").addEdge("c", "d")
                .addConditionalEdges("d", edge_async(state -> state.value("next_node", END)), Map.of("a", "a", END, END));

        CompiledGraph compiledGraph = stateGraph.compile();
        GraphRepresentation representation = compiledGraph.getGraph(GraphRepresentation.Type.MERMAID);
        System.out.println(representation.content());
        return compiledGraph;
    }

    @Bean("subStateGraph")
    public CompiledGraph subStateGraph() throws GraphStateException {
        KeyStrategyFactory keyStrategyFactory = () -> {
            Map<String, KeyStrategy> keyStrategyMap = new HashMap<>();
            keyStrategyMap.put("ids", KeyStrategy.MERGE);
            return keyStrategyMap;
        };

        var workflowChild = new StateGraph(keyStrategyFactory)
                .addNode("B1", node_async(state -> Map.of("ids", Map.of("b1", "B1"))))
                .addNode("B2", node_async(state -> Map.of("ids", Map.of("b2", "B2"))))
                .addEdge(START, "B1")
                .addEdge("B1", "B2")
                .addEdge("B2", END);

        var workflowParent = new StateGraph(keyStrategyFactory)
                .addNode("A", node_async(state -> Map.of("ids", Map.of("a", "A"))))
                .addNode("B", workflowChild)
                .addNode("C", node_async(state -> Map.of("ids", Map.of("c", "C"))))

                .addEdge(START, "A")
                .addEdge("A", "B")
                .addEdge("B", "C")
                .addEdge("C", END);

        return workflowParent.compile();
    }
}
