package com.zhouyu.interceptor;

import com.alibaba.cloud.ai.graph.agent.interceptor.ToolCallHandler;
import com.alibaba.cloud.ai.graph.agent.interceptor.ToolCallRequest;
import com.alibaba.cloud.ai.graph.agent.interceptor.ToolCallResponse;
import com.alibaba.cloud.ai.graph.agent.interceptor.ToolInterceptor;
import lombok.extern.log4j.Log4j2;

/**
 * 作者：IT周瑜
 * 公众号：IT周瑜
 * 微信号：it_zhouyu
 */
@Log4j2
public class ZhouyuToolInterceptor extends ToolInterceptor {
    @Override
    public ToolCallResponse interceptToolCall(ToolCallRequest request, ToolCallHandler handler) {
        long startTime = System.currentTimeMillis();
        try {
            ToolCallResponse response = handler.call(request);

            log.info("工具执行成功，耗时：{} ms， 请求：{}", System.currentTimeMillis() - startTime, request);
            return response;
        } catch (Exception e) {
            log.info("工具执行失败，耗时：{} ms， 请求：{}", System.currentTimeMillis() - startTime, request, e);
            return ToolCallResponse.of(request.getToolCallId(), request.getToolName(), "工具执行遇到问题，请稍后重试");
        }
    }

    @Override
    public String getName() {
        return "zhouyuToolInterceptor";
    }
}
