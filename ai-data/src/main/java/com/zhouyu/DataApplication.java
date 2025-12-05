package com.zhouyu;

import com.alibaba.cloud.ai.graph.CompiledGraph;
import com.alibaba.cloud.ai.graph.StateGraph;
import com.alibaba.cloud.ai.graph.action.AsyncNodeAction;
import com.alibaba.cloud.ai.graph.exception.GraphStateException;
import com.zhouyu.node.KeywordsExtractNode;
import com.zhouyu.node.TableInfoRecallNode;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import static com.alibaba.cloud.ai.graph.action.AsyncNodeAction.node_async;

/**
 * 作者：IT周瑜
 * 公众号：IT周瑜
 * 微信号：it_zhouyu
 */
@SpringBootApplication
public class DataApplication {

    @Bean
    public CompiledGraph compiledGraph(ChatClient.Builder chatClientBuilder, VectorStore vectorStore) throws GraphStateException {
        StateGraph stateGraph = new StateGraph();
        stateGraph.addNode("keywordsExtractNode", node_async(new KeywordsExtractNode(chatClientBuilder.build())));
        stateGraph.addNode("tableInfoRecallNode", node_async(new TableInfoRecallNode(vectorStore)));

        stateGraph.addEdge(StateGraph.START, "keywordsExtractNode")
                .addEdge("keywordsExtractNode", "tableInfoRecallNode")
                .addEdge("tableInfoRecallNode", StateGraph.END);

        return stateGraph.compile();
    }

    public static void main(String[] args) {
        SpringApplication.run(DataApplication.class, args);
    }
}
