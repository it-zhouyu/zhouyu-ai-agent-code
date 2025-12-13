package com.zhouyu.controller;

import com.zhouyu.service.OrderService;
import com.zhouyu.tool.OrderTool;
import org.springframework.ai.chat.client.ChatClient;
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
                你是一个专业的智能订单助手

                # 职责
                - 了解客户需求，推荐合适商品

                # 推荐商品注意事项
                - 推荐商品要符合用户需求
                - 推荐商品信息要完整
                - 至少推荐3个商品
                """;

        Flux<String> stream = chatClient.prompt()
                .system(systemPrompt)
                .user(question)
                .tools(orderTool)
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
