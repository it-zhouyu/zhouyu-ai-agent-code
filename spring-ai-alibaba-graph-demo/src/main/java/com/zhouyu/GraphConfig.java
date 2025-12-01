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
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.InMemoryChatMemoryRepository;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

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


}
