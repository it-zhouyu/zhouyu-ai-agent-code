package com.zhouyu;

import com.alibaba.cloud.ai.graph.agent.ReactAgent;
import com.alibaba.cloud.ai.graph.exception.GraphRunnerException;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.log4j.Log4j2;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * 作者：IT周瑜
 * 公众号：IT周瑜
 * 微信号：it_zhouyu
 */
@Service
@Log4j2
public class PlannerAgentService {


    @Autowired
    private ReactAgent plannerAgent;

    @Autowired
    private Map<String, ReactAgent> agentMap;

    public Plan plan(String prompt) {
        try {
            AssistantMessage assistantMessage = plannerAgent.call(prompt);
            String text = assistantMessage.getText();
            Plan plan = JSONObject.parseObject(text, Plan.class);
            return plan;
        } catch (GraphRunnerException e) {
            throw new RuntimeException(e);
        }
    }

    public String execute(Plan plan) {
        List<Step> steps = plan.getSteps();
        for (Step step : steps) {
            String stepNum = step.getStepNum();
            String agentName = step.getAgentName();
            ReactAgent reactAgent = agentMap.get(agentName);
            try {
                log.info("开始执行第{}步：{}", stepNum, step.getPrompt());
                AssistantMessage assistantMessage = reactAgent.call(step.getPrompt());
                String text = assistantMessage.getText();
                log.info("第{}步结果：{}", stepNum, text);
            } catch (GraphRunnerException e) {
                return "第" + stepNum + "步执行失败：" + e.getMessage();
            }
        }
        return "执行完毕";
    }
}
