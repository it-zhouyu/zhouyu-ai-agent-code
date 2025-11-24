package com.zhouyu;

import lombok.extern.log4j.Log4j2;
import org.springaicommunity.mcp.annotation.McpTool;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Service;

/**
 * 作者：IT周瑜
 * 公众号：IT周瑜
 * 微信号：it_zhouyu
 */
@Service
@Log4j2
public class WeatherService {

    @McpTool(description = "获取指定城市的天气")
    public String getWeather(String cityName) {
        log.info("正在获取天气信息...");
        if (cityName.equals("上海")) {
            return "天晴";
        } else if (cityName.equals("北京")) {
            return "下雨";
        }
        return "不知道";
    }
}
