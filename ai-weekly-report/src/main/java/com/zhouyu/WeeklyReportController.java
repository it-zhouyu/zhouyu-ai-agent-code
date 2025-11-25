package com.zhouyu;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import reactor.core.publisher.Flux;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;

/**
 * 作者：IT周瑜
 * 公众号：IT周瑜
 * 微信号：it_zhouyu
 */
@RestController
public class WeeklyReportController {

    private static final String SYSTEM_PROMPT = """
                ## 角色定义
                你是一名在软件开发公司工作的Java开发工程师。
                
                ## 任务
                你的任务是根据某个项目的Git提交历史记录，生成一份简洁、清晰、结构化的工作周报，并且发送邮件。

                ## 周报格式
                ------
                周报必须遵循以下格式：
                ## 本周项目总结 (YYYY-MM-DD 到 YYYY-MM-DD)

                ### 1. 主要功能与进展
                * [这里总结新功能和主要进展]

                ### 2. Bug 修复
                * [这里总结已修复的 Bug]

                ### 3. 代码重构与优化
                * [这里总结代码结构调整、性能优化等]

                ### 4. 其他
                * [总结文档更新、测试用例添加等其他提交]

                ## 下周计划
                * [你根据本周总结，生成大致的下周计划给用户进行参考]
                ------

                注意：
                1. 根据项目路径使用工具来获取Git提交历史记录，并生成周报内容（markdown格式）
                2. 周报内容中不要包含"Merge pull request"、"Merge branch"、".gitignore"等历史提交信息
                3. 周报内容中仅包含最核心最重要的提交记录信息即可
                4. "其他"中不要包含".gitignore"相关的内容
                5. 邮件发送需要将邮件内容从markdown格式转成对应的html格式
                6. 周报写完后，需要人类进行确认，询问人类是否发送邮件，确认之后才可以调用工具进行邮件发送
            """;

    @Autowired
    private ChatClient chatClient;

    @Autowired
    private ChatMemory chatMemory;

    @Autowired
    private EmailTool emailTool;

    @Autowired
    private GitTool gitTool;

    @GetMapping(value = "/sse")
    public SseEmitter sse(@RequestParam String chatId, @RequestParam String message) {

        SseEmitter sseEmitter = new SseEmitter() {
            @Override
            protected void extendResponse(ServerHttpResponse outputMessage) {
                HttpHeaders headers = outputMessage.getHeaders();
                headers.setContentType(new MediaType("text", "event-stream", StandardCharsets.UTF_8));
            }
        };

        Flux<String> stream = chatClient
                .prompt()
                .advisors(MessageChatMemoryAdvisor.builder(chatMemory).build())
                .advisors(a -> a.param(ChatMemory.CONVERSATION_ID, chatId))
                .system(SYSTEM_PROMPT)
                .user(message)
                .tools(gitTool, emailTool)
                .stream()
                .content();

        stream.subscribe(token -> {
            try {
                // 直接返回token会导致前端无法正确渲染（会吞空格），所以这里要加上content字段
//                 sseEmitter.send(token);
                sseEmitter.send(Map.of("content", token));
            } catch (IOException e) {
                sseEmitter.completeWithError(e);
            }
        }, sseEmitter::completeWithError, sseEmitter::complete);

        return sseEmitter;
    }
}
