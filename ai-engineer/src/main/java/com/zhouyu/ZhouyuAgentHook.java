package com.zhouyu;

import com.alibaba.cloud.ai.graph.OverAllState;
import com.alibaba.cloud.ai.graph.RunnableConfig;
import com.alibaba.cloud.ai.graph.agent.hook.AgentHook;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * 作者：IT周瑜
 * 公众号：IT周瑜
 * 微信号：it_zhouyu
 */
@Slf4j
public class ZhouyuAgentHook extends AgentHook {
    @Override
    public String getName() {
        return "zhouyuAgentHook";
    }

    @Override
    public CompletableFuture<Map<String, Object>> beforeAgent(OverAllState state, RunnableConfig config) {
        String agentName = config.metadata("_AGENT_").orElseThrow().toString();

        if (agentName.contains("architectAgent")) {
            agentName = "架构师";
        } else if (agentName.contains("backendAgent")) {
            agentName = "后端";
        } else if (agentName.contains("frontendAgent")) {
            agentName = "前端";
        } else if (agentName.contains("reviewAgent")) {
            agentName = "代码审核";
        }

        log.info("{} Agent开始执行", agentName);

        return super.beforeAgent(state, config);
    }

    @Override
    public CompletableFuture<Map<String, Object>> afterAgent(OverAllState state, RunnableConfig config) {
        String agentName = config.metadata("_AGENT_").orElseThrow().toString();

        if (agentName.contains("architectAgent")) {
            agentName = "架构师";
        } else if (agentName.contains("backendAgent")) {
            agentName = "后端";
        } else if (agentName.contains("frontendAgent")) {
            agentName = "前端";
        } else if (agentName.contains("reviewAgent")) {
            agentName = "代码审核";
        }

        log.info("{} Agent执行结束", agentName);

        return super.afterAgent(state, config);
    }
}
