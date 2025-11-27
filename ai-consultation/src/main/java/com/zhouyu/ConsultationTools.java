package com.zhouyu;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

/**
 * 作者：IT周瑜
 * 公众号：IT周瑜
 * 微信号：it_zhouyu
 */
@Component
public class ConsultationTools {

    @Tool(description = "进行挂号")
    public String register(@ToolParam(description = "科室名称") String departmentName) {
        System.out.println("进行挂号: " + departmentName);
        return "挂号成功，请等待医生处理";
    }
}
