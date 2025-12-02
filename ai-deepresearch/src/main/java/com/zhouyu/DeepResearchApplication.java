package com.zhouyu;

import com.alibaba.cloud.ai.graph.CompiledGraph;
import com.alibaba.cloud.ai.graph.OverAllState;
import com.alibaba.cloud.ai.graph.StateGraph;
import com.alibaba.cloud.ai.graph.action.AsyncEdgeAction;
import com.alibaba.cloud.ai.graph.action.AsyncNodeAction;
import com.alibaba.cloud.ai.graph.action.EdgeAction;
import com.alibaba.cloud.ai.graph.exception.GraphStateException;
import com.zhouyu.node.CoordinatorNode;
import com.zhouyu.node.PlannerNode;
import com.zhouyu.node.ReporterNode;
import com.zhouyu.node.ResearcherNode;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.util.Map;

import static com.alibaba.cloud.ai.graph.StateGraph.END;
import static com.alibaba.cloud.ai.graph.StateGraph.START;
import static com.alibaba.cloud.ai.graph.action.AsyncNodeAction.node_async;

/**
 * 作者：IT周瑜
 * 公众号：IT周瑜
 * 微信号：it_zhouyu
 */
@SpringBootApplication
public class DeepResearchApplication {

    // 意图识别的节点
    // 制定研究计划的节点
    // 执行步骤的节点
    // 生成报告的节点

    @Bean
    public CompiledGraph compiledGraph(ChatClient.Builder chatClientBuilder) throws GraphStateException {

        StateGraph stateGraph = new StateGraph();
        stateGraph.addNode("coordinatorNode", node_async(new CoordinatorNode(chatClientBuilder.build())));
        stateGraph.addNode("plannerNode", node_async(new PlannerNode(chatClientBuilder.build())));
        stateGraph.addNode("researcherNode", node_async(new ResearcherNode(chatClientBuilder.build())));
        stateGraph.addNode("reporterNode", node_async(new ReporterNode(chatClientBuilder.build())));

        stateGraph.addEdge(START, "coordinatorNode")
                .addConditionalEdges("coordinatorNode", AsyncEdgeAction.edge_async(state -> {
                    AssistantMessage coordinatorResult = (AssistantMessage)state.value("coordinatorResult").orElseThrow();
                    return coordinatorResult.getText().equals("NEED_PLAN") ? "plannerNode" : END;
                }), Map.of("plannerNode", "plannerNode", END, END))
                .addEdge("plannerNode", "researcherNode")
                .addEdge("researcherNode", "reporterNode")
                .addEdge("reporterNode", END);

        return stateGraph.compile();
    }

    public static void main(String[] args) {
        SpringApplication.run(DeepResearchApplication.class, args);
    }
}
