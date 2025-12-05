package com.zhouyu;

import com.alibaba.cloud.ai.graph.*;
import com.alibaba.cloud.ai.graph.action.AsyncEdgeAction;
import com.alibaba.cloud.ai.graph.action.AsyncNodeAction;
import com.alibaba.cloud.ai.graph.action.EdgeAction;
import com.alibaba.cloud.ai.graph.exception.GraphStateException;
import com.zhouyu.node.*;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.util.Map;

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
        stateGraph.addNode("plannerNode", node_async(new PlannerNode(chatClientBuilder.build())));
        stateGraph.addNode("planExecuteNode", node_async(new PlanExecuteNode(chatClientBuilder.build())));
        stateGraph.addNode("sqlExecuteNode", node_async(new SqlExecuteNode()));
        stateGraph.addNode("reportGeneratorNode", node_async(new ReportGeneratorNode(chatClientBuilder.build())));


        stateGraph.addEdge(StateGraph.START, "keywordsExtractNode")
                .addEdge("keywordsExtractNode", "tableInfoRecallNode")
                .addEdge("tableInfoRecallNode", "plannerNode")
                .addEdge("plannerNode", "planExecuteNode")
                .addConditionalEdges("planExecuteNode", AsyncEdgeAction.edge_async(new EdgeAction() {
                    @Override
                    public String apply(OverAllState state) throws Exception {
                        return state.value("planExecuteNextNode", String.class).orElseThrow();
                    }
                }), Map.of("sql", "sqlExecuteNode", "report", "reportGeneratorNode"))
                .addEdge("sqlExecuteNode", "planExecuteNode")
                .addEdge("reportGeneratorNode", StateGraph.END);

        CompiledGraph compiledGraph = stateGraph.compile(CompileConfig.builder()
                        .interruptAfter("plannerNode")
                .build());
        GraphRepresentation representation = compiledGraph.getGraph(GraphRepresentation.Type.MERMAID);
        System.out.println(representation.content());

        return compiledGraph;
    }

    public static void main(String[] args) {
        SpringApplication.run(DataApplication.class, args);
    }
}
