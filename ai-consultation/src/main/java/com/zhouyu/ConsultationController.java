package com.zhouyu;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.rag.advisor.RetrievalAugmentationAdvisor;
import org.springframework.ai.rag.retrieval.search.VectorStoreDocumentRetriever;
import org.springframework.ai.vectorstore.VectorStore;
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
public class ConsultationController {

    @Autowired
    private VectorStore vectorStore;

    @Autowired
    private ChatMemory chatMemory;

    @Autowired
    private ChatClient.Builder chatClientBuilder;

    private static final String SYSTEM_PROMPT = """
            ## 角色
            你是一个专业、贴心的AI问诊助手
            
            ## 角色任务
            通过与用户进行**最多5步**的交流，快速、准确地分析他们的症状，并从提供的科室信息知识库中，为他们推荐最匹配的科室，如果你不能准确的确定应该挂哪个科，你应该继续询问病人的其他症状，来帮助你确认应该挂哪个科
       
            ## 注意
            1、你不是医生。严禁提供任何形式的医学诊断、病情分析、治疗建议或药物推荐。
            2、所有的提问都必须是*为了判断科室*而服务的。
            3、整个对话必须在5个交互回合内完成。你必须高效地主导对话。
            4、询问病人症状的时候用markdown格式进行回复
            """;


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

        RetrievalAugmentationAdvisor retrievalAugmentationAdvisor = RetrievalAugmentationAdvisor.builder()
                .documentRetriever(VectorStoreDocumentRetriever.builder()
                        .topK(3)
                        .similarityThreshold(0.3)
                        .vectorStore(vectorStore)
                        .build())
                .queryExpander(new ConsultationQueryExpander())
                .build();

        Flux<String> stream = chatClient.prompt()
                .advisors(retrievalAugmentationAdvisor)
                .advisors(MessageChatMemoryAdvisor.builder(chatMemory).build())
                .advisors(advisorSpec -> advisorSpec.param(ChatMemory.CONVERSATION_ID, chatId))
                .system(SYSTEM_PROMPT)
                .user(question)
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
}
