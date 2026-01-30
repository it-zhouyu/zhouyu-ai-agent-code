package com.zhouyu.tools;

import io.agentscope.core.tool.Tool;
import io.agentscope.core.tool.ToolParam;

/**
 * 作者：IT周瑜
 * 公众号：IT周瑜
 * 微信号：it_zhouyu
 */
public class EmailService {

    @Tool(description = "发送邮件")
    public String send(
            @ToolParam(name = "to") String to,
            @ToolParam(name = "subject") String subject,
            @ToolParam(name = "apiKey") String apiKey) {  // 预设，LLM 不可见
        System.out.println("发送邮件：" + to + " " + subject);
        System.out.println("apiKey：" + apiKey);
        return "已发送";
    }
}
