package com.zhouyu.node;

import com.alibaba.cloud.ai.graph.OverAllState;
import com.alibaba.cloud.ai.graph.action.NodeAction;
import com.alibaba.fastjson2.JSON;
import com.zhouyu.dto.Plan;
import com.zhouyu.dto.TableInfo;
import com.zhouyu.util.PromptUtil;
import org.springframework.ai.chat.client.ChatClient;

import java.util.List;
import java.util.Map;

/**
 * 作者：IT周瑜
 * 公众号：IT周瑜
 * 微信号：it_zhouyu
 */
public class PlannerNode implements NodeAction {

    private ChatClient chatClient;

    public PlannerNode(ChatClient chatClient) {
        this.chatClient = chatClient;
    }

    @Override
    public Map<String, Object> apply(OverAllState state) throws Exception {

        // 根据用户的输入需求，以及前面节点匹配出来的表信息和字段信息，构建一个执行计划

        String input = state.value("input", String.class).orElseThrow();
        List<TableInfo> tableInfoList = state.value("tableInfoRecallResult", List.class).orElseThrow();

        // 定义提示词
        String prompt = PromptUtil.getPrompt("planner");
        String systemPrompt = prompt.replace("{{table_infos}}", JSON.toJSONString(tableInfoList));

        String result = chatClient.prompt().system(systemPrompt).user(input).call().content();
        Plan plan = JSON.parseObject(result, Plan.class);
        return Map.of("plannerResult", plan);
    }
}
