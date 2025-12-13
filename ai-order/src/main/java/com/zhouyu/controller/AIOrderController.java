package com.zhouyu.controller;

import com.zhouyu.service.OrderService;
import com.zhouyu.tool.OrderTool;
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
public class AIOrderController {

    @Autowired
    private ChatClient.Builder chatClientBuilder;

    @Autowired
    private OrderTool orderTool;

    @Autowired
    private OrderService orderService;

    @Autowired
    private ChatMemory chatMemory;

    @GetMapping("/sse")
    public SseEmitter sse(@RequestParam("chatId") String chatId, @RequestParam("question") String question) {

        ChatClient chatClient = chatClientBuilder.build();

        SseEmitter sseEmitter = new SseEmitter() {
            @Override
            protected void extendResponse(ServerHttpResponse outputMessage) {
                HttpHeaders headers = outputMessage.getHeaders();
                headers.setContentType(new MediaType("text", "event-stream", StandardCharsets.UTF_8));
            }
        };

        String systemPrompt = """
                你是一个专业的智能订单助手，帮助客户自动创建订单、支付订单、退款等操作。

                # 职责
                - 先理解客户需求
                - 根据客户需求从商品库中查找并推荐合适商品
                - 客户确定了购买的商品后帮助客户创建订单

                # 推荐商品注意事项
                - 推荐商品要符合用户需求
                - 推荐商品信息要完整
                - 至少推荐3个商品
                - 只有在不确定用户需求时才推荐商品，如果用户已经确定了需求则不要推荐
                
                # 创建订单注意事项
                - 创建订单前如果信息不足可以寻问客户补充信息
                - 创建订单前一定要让客户先确认订单信息
                
                # 工具调用注意事项
                - 推荐商品信息工具（searchProducts）只有在不确定用户需求时才调用，如果用户已经确定了需求则不需要调用
                - 创建订单工具（createOrder）必须在用户确认了订单信息后才能调用
                
                # 其他注意事项
                - 不要编造其他商品，只能基于商品库中的商品信息回答问题或创建订单
                
                # 用户基本信息
                - 用户ID: 1010
                - 用户名: 周瑜
                """;

        Flux<String> stream = chatClient.prompt()
                .system(systemPrompt)
                .user(question)
                .tools(orderTool)
                .advisors(MessageChatMemoryAdvisor.builder(chatMemory).build())
                .advisors(advisorSpec -> advisorSpec.param(ChatMemory.CONVERSATION_ID, chatId))
                .stream()
                .content();

        stream.subscribe(token -> {
            try {
                // 直接返回token会导致前端无法正确渲染（会吞空格），所以这里要加上content字段
                // sseEmitter.send(token);
                sseEmitter.send(Map.of("content", token));
            } catch (IOException e) {
                sseEmitter.completeWithError(e);
            }
        }, sseEmitter::completeWithError, sseEmitter::complete);

        return sseEmitter;
    }

    @GetMapping("/initProduct")
    public String initProduct() {
        orderService.initProductVector();
        return "success";
    }
}
