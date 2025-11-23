package com.zhouyu;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * 作者：IT周瑜
 * 公众号：IT周瑜
 * 微信号：it_zhouyu
 */
@Component
public class ZhouyuTools {

    @Tool(description = "获取当前时间")
    LocalDateTime getCurrentDateTime() {
        System.out.println("获取当前时间");
        throw new RuntimeException("获取当前时间异常");
//        return LocalDateTime.now();
    }

    @Tool(description = "用指定时间设置闹钟")
    void setAlarm(AlarmRequest alarmRequest) {
        System.out.println("地址：" + alarmRequest.getAddress());
        System.out.println("闹钟时间为：" + alarmRequest.getTime());
    }
}
