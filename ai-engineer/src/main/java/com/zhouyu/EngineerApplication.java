package com.zhouyu;

import com.alibaba.cloud.ai.graph.NodeOutput;
import com.alibaba.cloud.ai.graph.agent.ReactAgent;
import com.alibaba.cloud.ai.graph.agent.extension.interceptor.FilesystemInterceptor;
import com.alibaba.cloud.ai.graph.agent.extension.tools.filesystem.ReadFileTool;
import com.alibaba.cloud.ai.graph.exception.GraphRunnerException;
import com.alibaba.cloud.ai.graph.streaming.StreamingOutput;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.support.ToolCallbacks;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import reactor.core.publisher.Flux;

/**
 * 作者：IT周瑜
 * 公众号：IT周瑜
 * 微信号：it_zhouyu
 */
@Slf4j
@SpringBootApplication
public class EngineerApplication {

    @Bean
    public ReactAgent architectAgent(ChatModel chatModel) {
        return ReactAgent.builder()
                .name("architectAgent")
                .model(chatModel)
                .tools(ToolCallbacks.from(new FileTool()))
                .systemPrompt("""
                        你是一名资深架构师，负责进行架构设计
                        
                        # 职责
                        - 根据用户需求进行架构设计
                        - 主要做技术选型、接口设计、表设计
                        - 如果涉及到前端，就采用前后端分离
                        
                        # 注意
                        - 你只需要生成架构设计方案，不需要实现具体的前端和后端代码
                        - 项目工作目录为：{workspace}
                        - 你只需要将架构设计方案保存到本地文件系统，不需要实现前端和后端代码，更不需要去保存代码
                        """.replace("{workspace}", "./zhouyu-code"))
                .build();
    }


    public static void main(String[] args) {
        ConfigurableApplicationContext applicationContext = SpringApplication.run(EngineerApplication.class, args);
        ReactAgent architectAgent = applicationContext.getBean("architectAgent", ReactAgent.class);

        try {
            Flux<NodeOutput> outputFlux = architectAgent.stream("开发一个最简单的登录功能，使用Java和Vue.js，不需要考虑安全");
            outputFlux.subscribe(output -> {
                if (output instanceof StreamingOutput<?> streamingOutput) {
                    if (streamingOutput.message() != null) {
                        System.out.print(streamingOutput.message().getText());
                    }
                }
            });
        } catch (GraphRunnerException e) {
            throw new RuntimeException(e);
        }


    }
}
