package com.zhouyu.server;

import lombok.extern.log4j.Log4j2;
import org.springframework.ai.tool.annotation.Tool;

/**
 * 作者：IT周瑜
 * 公众号：IT周瑜
 * 微信号：it_zhouyu
 */
@Log4j2
public class ZhouyuTools {

    @Tool(description = "获取指定城市的天气")
    public String getWeather(String cityName) {
        log.info("正在获取{}天气信息...", cityName);
        return "天晴";
    }
}
