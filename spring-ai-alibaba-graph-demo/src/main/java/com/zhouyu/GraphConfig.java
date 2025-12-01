package com.zhouyu;

import com.alibaba.cloud.ai.graph.CompiledGraph;
import com.alibaba.cloud.ai.graph.StateGraph;
import com.alibaba.cloud.ai.graph.action.AsyncCommandAction;
import com.alibaba.cloud.ai.graph.exception.GraphStateException;
import com.zhouyu.blog.ContentNodeAction;
import com.zhouyu.blog.TitleNodeAction;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static com.alibaba.cloud.ai.graph.StateGraph.END;
import static com.alibaba.cloud.ai.graph.StateGraph.START;
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

        return compiledGraph;
    }
}
