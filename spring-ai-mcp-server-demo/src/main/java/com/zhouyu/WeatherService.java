package com.zhouyu;

import io.modelcontextprotocol.spec.McpSchema;
import lombok.extern.log4j.Log4j2;
import org.springaicommunity.mcp.annotation.McpArg;
import org.springaicommunity.mcp.annotation.McpPrompt;
import org.springaicommunity.mcp.annotation.McpResource;
import org.springaicommunity.mcp.annotation.McpTool;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 作者：IT周瑜
 * 公众号：IT周瑜
 * 微信号：it_zhouyu
 */
@Service
@Log4j2
public class WeatherService {

    @Autowired
    private Environment environment;

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

    @McpPrompt(name = "greeting", description = "欢迎语")
    public McpSchema.GetPromptResult greeting(@McpArg(name = "name") String name) {
        String message = "你好, " + name + "! 有什么可以帮您?";
        return new McpSchema.GetPromptResult("Greeting", List.of(new McpSchema.PromptMessage(McpSchema.Role.ASSISTANT, new McpSchema.TextContent(message))));
    }

    @McpResource(uri = "config://{key}", name = "configuration")
    public String getConfig(String key) {
        return environment.getProperty(key, "123");
    }
}
