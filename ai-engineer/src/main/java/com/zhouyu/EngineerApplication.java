package com.zhouyu;

import com.alibaba.cloud.ai.graph.NodeOutput;
import com.alibaba.cloud.ai.graph.agent.ReactAgent;
import com.alibaba.cloud.ai.graph.agent.extension.interceptor.FilesystemInterceptor;
import com.alibaba.cloud.ai.graph.agent.extension.tools.filesystem.ReadFileTool;
import com.alibaba.cloud.ai.graph.agent.flow.agent.SequentialAgent;
import com.alibaba.cloud.ai.graph.agent.interceptor.contextediting.ContextEditingInterceptor;
import com.alibaba.cloud.ai.graph.exception.GraphRunnerException;
import com.alibaba.cloud.ai.graph.exception.GraphStateException;
import com.alibaba.cloud.ai.graph.streaming.StreamingOutput;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.support.ToolCallbacks;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import reactor.core.publisher.Flux;

import java.util.List;

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
                .hooks(new ZhouyuAgentHook())
                .systemPrompt("""
                        你是一名资深架构师，负责进行架构设计
                        
                        # 职责
                        - 根据用户需求进行架构设计
                        - 主要做技术选型、接口设计、表设计
                        - 如果涉及到前端，就采用前后端分离的架构
                        - 代码工程目录也要区分前端和后端
                        
                        # 注意
                        - 你只需要生成架构设计方案，不需要实现具体的前端和后端代码
                        - 项目工作目录为：{workspace}
                        - 你只需要将架构设计方案保存到本地文件系统，不需要实现前端和后端代码，更不需要去保存代码
                        - 如果项目工作目录下已经有了架构设计方案，就不需要再生成了
                        - 逐个调用工具，不要一次性调用多个工具
                        
                        """.replace("{workspace}", "./zhouyu-code"))
                .build();
    }

    @Bean
    public ReactAgent backendAgent(ChatModel chatModel) {
        return ReactAgent.builder()
                .name("backendAgent")
                .model(chatModel)
                .tools(ToolCallbacks.from(new FileTool()))
                .hooks(new ZhouyuAgentHook())
                .systemPrompt("""
                        你是一名资深后端开发工程师，负责实现后端代码
                        
                        # 职责
                        - 根据用户需求和架构方案开发后端代码
                        - 你必须从项目工作目录中读取架构设计方案来实现后端代码
                        - 将你写的后端代码保存到本地项目工作目录中
                        
                        # 注意
                        - 项目工作目录为：{workspace}
                        - 逐个调用工具，不要一次性调用多个工具
                        - 你只需要写后端代码，不需要写前端代码
                        """.replace("{workspace}", "./zhouyu-code"))
                .build();
    }

    @Bean
    public ReactAgent frontendAgent(ChatModel chatModel) {
        ContextEditingInterceptor contextEditingInterceptor = ContextEditingInterceptor.builder()
                .trigger(1000)
                .clearToolInputs(true)
                .build();

        return ReactAgent.builder()
                .name("frontendAgent")
                .model(chatModel)
                .tools(ToolCallbacks.from(new FileTool()))
                .interceptors(contextEditingInterceptor)
                .hooks(new ZhouyuAgentHook())
                .systemPrompt("""
                        你是一名资深前端开发工程师，负责实现前端代码
                        
                        # 职责
                        - 根据用户需求和架构方案开发前端代码，不需要读取后端代码
                        - 你必须从项目工作目录中读取架构设计方案来实现前端代码
                        - 将你写的前端代码保存到本地项目工作目录中
                        
                        # 注意
                        - 项目工作目录为：{workspace}
                        - 逐个调用工具，不要一次性调用多个工具
                        - 你只需要写前端代码，不需要写后端代码
                        """.replace("{workspace}", "./zhouyu-code"))
                .build();
    }

    // React

    @Bean
    public ReactAgent reviewAgent(ChatModel chatModel) {
        ContextEditingInterceptor contextEditingInterceptor = ContextEditingInterceptor.builder()
                .trigger(1000)
                .clearToolInputs(true)
                .build();

        return ReactAgent.builder()
                .name("reviewAgent")
                .model(chatModel)
                .tools(ToolCallbacks.from(new FileTool()))
                .interceptors(contextEditingInterceptor)
                .hooks(new ZhouyuAgentHook())
                .systemPrompt("""
                        你是一名资深代码Review工程师
                        
                        # 职责
                        - 只对后端代码进行Review
                        - 生成Review报告并保存到本地文件系统
                        
                        # 注意
                        - 项目工作目录为：{workspace}
                        - 逐个调用工具，不要一次性调用多个工具
                        """.replace("{workspace}", "./zhouyu-code"))
                .build();
    }

    @Bean
    public SequentialAgent sequentialAgent(ReactAgent architectAgent, ReactAgent backendAgent, ReactAgent frontendAgent, ReactAgent reviewAgent) throws GraphStateException {
        return SequentialAgent.builder()
                .name("sequentialAgent")
                .subAgents(List.of(architectAgent, backendAgent, frontendAgent, reviewAgent))
                .build();
    }

    @Bean
    public ReactAgent plannerAgent(ChatModel chatModel) {
        return ReactAgent.builder()
                .name("plannerAgent")
                .model(chatModel)
                .systemPrompt("""
                        你是一名高级系统规划师，主要职责是根据提供的Agent信息和用户需求生成具体的执行计划：
                        
                        可用的Agent信息：
                        - architectAgent: 负责系统架构设计、技术选型、接口设计
                        - backendAgent: 负责后端应用开发
                        - frontendAgent: 负责前端应用开发
                        - reviewAgent: 负责代码审查
                        
                        注意：
                        - 输出结果中不要包含```json
                        - 除开architectAgent需要生成架构方案，其他Agent都只需要读取架构方案，不要自己再生成架构方案
                        
                        输出格式（必须是有效的JSON对象）:
                          ```java
                                public class Plan {
                                  private List<Step> steps;
                                }
                                public class Step {
                                   private String stepNum;
                                   private String agentName;
                                   private String prompt;  // 输入给子Agent的提示词，不需要很详细，只需要告诉子Agent要根据架构方案来实现
                                }
                          ```
                        """)
                .build();
    }

    public static void main(String[] args) {
        ConfigurableApplicationContext applicationContext = SpringApplication.run(EngineerApplication.class, args);
//        ReactAgent architectAgent = applicationContext.getBean("architectAgent", ReactAgent.class);
//        SequentialAgent sequentialAgent = applicationContext.getBean("sequentialAgent", SequentialAgent.class);

//        try {
////            Flux<NodeOutput> outputFlux = architectAgent.stream("开发一个最简单的登录功能，使用Java和Vue.js，不需要考虑安全");
//            Flux<NodeOutput> outputFlux = sequentialAgent.stream("开发一个最简单的登录功能，使用Java和Vue.js，不需要考虑安全");
//            outputFlux.subscribe(output -> {
//                if (output instanceof StreamingOutput<?> streamingOutput) {
//                    if (streamingOutput.message() != null) {
//                        System.out.print(streamingOutput.message().getText());
//                    }
//                }
//            });
//        } catch (GraphRunnerException e) {
//            throw new RuntimeException(e);
//        }


        PlannerAgentService plannerAgentService = applicationContext.getBean(PlannerAgentService.class);
        Plan plan = plannerAgentService.plan("开发一个最简单的登录功能，使用Java和Vue.js，不需要考虑安全");
        System.out.println(plan);

        String result = plannerAgentService.execute(plan);
        System.out.println(result);

    }
}
