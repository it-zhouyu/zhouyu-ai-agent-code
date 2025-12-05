package com.zhouyu.node;

import com.alibaba.cloud.ai.graph.OverAllState;
import com.alibaba.cloud.ai.graph.action.NodeAction;
import com.alibaba.fastjson2.JSON;
import com.zhouyu.dto.Plan;
import com.zhouyu.dto.Step;
import com.zhouyu.dto.StepResultDto;
import lombok.extern.log4j.Log4j2;
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
@Log4j2
public class SqlExecuteNode implements NodeAction {
    @Override
    public Map<String, Object> apply(OverAllState state) throws Exception {

        Plan plan = state.value("plannerResult", Plan.class).orElseThrow();
        Integer currentStepNum = state.value("currentStepNum", 0);
        Step step = plan.getSteps().get(currentStepNum);

        // 执行sql
        String url = "jdbc:mysql://localhost:3306/zhouyu_db";
        String username = "root";
        String password = "Zhouyu123456...";
        DataSource dataSource = createDataSource(url, username, password);

        StepResultDto stepResultDto = new StepResultDto();
        stepResultDto.setStep(step);
        try {
            JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
            log.info("执行SQL：{}", step.getSql());
            List<Map<String, Object>> result = jdbcTemplate.queryForList(step.getSql());
            stepResultDto.setSuccess(true);
            stepResultDto.setData(JSON.toJSONString(result));
        } catch (Exception e) {
            stepResultDto.setSuccess(false);
            stepResultDto.setData("执行SQL失败：" + e.getMessage());
            log.error("执行SQL失败", e);
        }

        // 将当前步骤的结果保存起来
        HashMap<Integer, StepResultDto> planExecuteResult = state.value("planExecuteResult", new HashMap<Integer, StepResultDto>());
        planExecuteResult.put(currentStepNum, stepResultDto);

        return Map.of("planExecuteResult", planExecuteResult);
    }

    public DataSource createDataSource(String url, String username, String password) {
        return DataSourceBuilder.create()
                .driverClassName("com.mysql.cj.jdbc.Driver") // 硬编码为 MySQL 驱动
                .url(url)
                .username(username)
                .password(password)
                .build();
    }
}
