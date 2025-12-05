package com.zhouyu.node;

import com.alibaba.cloud.ai.graph.OverAllState;
import com.alibaba.cloud.ai.graph.action.NodeAction;
import com.alibaba.fastjson2.JSON;
import com.zhouyu.dto.Plan;
import com.zhouyu.dto.Step;
import com.zhouyu.dto.StepResultDto;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 作者：IT周瑜
 * 公众号：IT周瑜
 * 微信号：it_zhouyu
 */
public class PlanExecuteNode implements NodeAction {

    private ChatClient chatClient;

    public PlanExecuteNode(ChatClient chatClient) {
        this.chatClient = chatClient;
    }

    @Override
    public Map<String, Object> apply(OverAllState state) throws Exception {

        Plan plan = state.value("plannerResult", Plan.class).orElseThrow();
        Integer currentStepNum = state.value("currentStepNum", 0);

        // 判断当前步骤是否执行成功
        HashMap<Integer, StepResultDto> planExecuteResult = state.value("planExecuteResult", new HashMap<Integer, StepResultDto>());
        StepResultDto stepResultDto = planExecuteResult.get(currentStepNum);
        if (stepResultDto == null) {
            // 表示当前步骤还没有执行，那么则去执行
            return Map.of("planExecuteNextNode", "sql", "currentStepNum", currentStepNum);
        } else {
            // 表示当前步骤已经执行过了

            if (stepResultDto.isSuccess()) {
                // 表示当前步骤执行成功，则继续执行下一个步骤
                currentStepNum++;
                String nextNode = "sql";
                if (currentStepNum >= plan.getSteps().size()) {
                    nextNode = "report";
                }
                return Map.of("planExecuteNextNode", nextNode, "currentStepNum", currentStepNum);
            } else {

                // 表示当前步骤执行失败，则调用大模型让它帮忙修改sql
                String prompt = """
                        以下sql执行出错，请帮我修改，并返回修改后的sql，只需要返回sql即可，我要直接用来执行
                        
                        原始sql：
                        {originSql}
                        
                        错误信息：
                        {errorMsg}
                        
                        修改后sql:
                        """;
                String sqlPrompt = new PromptTemplate(prompt).render(Map.of("originSql", stepResultDto.getStep().getSql(), "errorMsg", stepResultDto.getData()));

                String finalSql = chatClient.prompt().user(sqlPrompt).call().content();
                plan.getSteps().get(currentStepNum).setSql(finalSql); // 将修改后的sql更新到step中，后面继续执行

                return Map.of("planExecuteNextNode", "sql", "currentStepNum", currentStepNum, "plannerResult", plan);
            }
        }
    }


}
