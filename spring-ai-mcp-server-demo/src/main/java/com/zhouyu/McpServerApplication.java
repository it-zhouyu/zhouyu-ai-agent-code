package com.zhouyu;

import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.tool.method.MethodToolCallbackProvider;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

/**
 * 作者：IT周瑜
 * 公众号：IT周瑜
 * 微信号：it_zhouyu
 */
@SpringBootApplication
public class McpServerApplication {

//    @Bean
//    public ToolCallbackProvider weatherTools(WeatherService weatherService) {
//        return MethodToolCallbackProvider.builder().toolObjects(weatherService).build();
//    }

    public static void main(String[] args) {
        SpringApplication.run(McpServerApplication.class, args);
    }
}
