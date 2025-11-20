package com.zhouyu;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.converter.BeanOutputConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.bind.annotation.GetMapping;
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
public class ZhouyuController {

    @Autowired
    private ChatClient chatClient;

    @GetMapping("/chat")
    public String chat(String question) {
        return chatClient.prompt(question).call().content();
    }

    @GetMapping(value = "/stream", produces = "text/html;charset=UTF-8")
    public Flux<String> stream(String question) {
        // 数据流
        return chatClient.prompt(question).stream().content();
    }

    @GetMapping(value = "/sse")
    public SseEmitter sse(String question) {
        SseEmitter sseEmitter = new SseEmitter() {
            @Override
            protected void extendResponse(ServerHttpResponse outputMessage) {
                HttpHeaders headers = outputMessage.getHeaders();
                headers.setContentType(new MediaType("text", "event-stream", StandardCharsets.UTF_8));
            }
        };

        Flux<String> stream = chatClient.prompt(question).stream().content();

        stream.subscribe(token -> {
            try {
                sseEmitter.send(token);
            } catch (IOException e) {
                sseEmitter.completeWithError(e);
            }
        }, sseEmitter::completeWithError, sseEmitter::complete);

        return sseEmitter;
    }

    @GetMapping("/system")
    public String system(String question) {
        return this.chatClient
                .prompt()
                .system("你是周瑜老师")
                .user(question)
                .call()
                .content();
    }

    @GetMapping("/output")
    public Poem output(String topic) {

        BeanOutputConverter<Poem> outputConverter = new BeanOutputConverter<>(new ParameterizedTypeReference<Poem>(){});

        PromptTemplate promptTemplate = new PromptTemplate("写一首关于{topic}的七言绝句，{format}");

        String prompt = promptTemplate.render(Map.of("topic", topic, "format", outputConverter.getFormat()));

        String content = chatClient.prompt(prompt).call().content();

        return outputConverter.convert(content);
    }

    @GetMapping("/entity")
    public Poem entity(String topic) {

        PromptTemplate promptTemplate = new PromptTemplate("写一首关于{topic}的七言绝句");
        String prompt = promptTemplate.render(Map.of("topic", topic));

        return chatClient.prompt(prompt).call().entity(Poem.class);
    }

    static class Poem {
        private String title;
        private String author;
        private String content;

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public String getAuthor() {
            return author;
        }

        public void setAuthor(String author) {
            this.author = author;
        }

        public String getContent() {
            return content;
        }

        public void setContent(String content) {
            this.content = content;
        }
    }
}
