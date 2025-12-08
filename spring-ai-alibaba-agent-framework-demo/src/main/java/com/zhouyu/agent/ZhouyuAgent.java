package com.zhouyu.agent;

import com.alibaba.cloud.ai.graph.CompileConfig;
import com.alibaba.cloud.ai.graph.KeyStrategy;
import com.alibaba.cloud.ai.graph.KeyStrategyFactory;
import com.alibaba.cloud.ai.graph.StateGraph;
import com.alibaba.cloud.ai.graph.agent.Agent;
import com.alibaba.cloud.ai.graph.agent.ReactAgent;
import com.alibaba.cloud.ai.graph.agent.flow.agent.FlowAgent;
import com.alibaba.cloud.ai.graph.agent.flow.builder.FlowGraphBuilder;
import com.alibaba.cloud.ai.graph.agent.flow.node.TransparentNode;
import com.alibaba.cloud.ai.graph.agent.flow.strategy.FlowGraphBuildingStrategy;
import com.alibaba.cloud.ai.graph.exception.GraphStateException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.alibaba.cloud.ai.graph.StateGraph.END;
import static com.alibaba.cloud.ai.graph.StateGraph.START;
import static com.alibaba.cloud.ai.graph.action.AsyncNodeAction.node_async;

/**
 * 作者：IT周瑜
 * 公众号：IT周瑜
 * 微信号：it_zhouyu
 */
public class ZhouyuAgent extends FlowAgent {

    public ZhouyuAgent(String name, String description, CompileConfig compileConfig, List<Agent> subAgents) throws GraphStateException {
        super(name, description, compileConfig, subAgents);
    }

    @Override
    protected StateGraph buildSpecificGraph(FlowGraphBuilder.FlowGraphConfig config) throws GraphStateException {

        StateGraph stateGraph = new StateGraph(config.getName(), new KeyStrategyFactory() {
            @Override
            public Map<String, KeyStrategy> apply() {

                HashMap<String, KeyStrategy> keyStrategyMap = new HashMap<>();
                keyStrategyMap.put("messages", KeyStrategy.APPEND);

                return keyStrategyMap;
            }
        });

        Agent rootAgent = config.getRootAgent();

        stateGraph.addNode(rootAgent.name(), node_async(new TransparentNode()));

        stateGraph.addEdge(START, rootAgent.name());

        Agent currentAgent = rootAgent;
        for (Agent subAgent : config.getSubAgents()) {
            if (subAgent instanceof ReactAgent reactAgent) {
                stateGraph.addNode(subAgent.name(), reactAgent.asNode(reactAgent.isIncludeContents(), reactAgent.isReturnReasoningContents(), reactAgent.getOutputKey()));
            }
            stateGraph.addEdge(currentAgent.name(), subAgent.name());
            currentAgent = subAgent;
        }

        stateGraph.addEdge(currentAgent.name(), END);

        return stateGraph;
    }
}
