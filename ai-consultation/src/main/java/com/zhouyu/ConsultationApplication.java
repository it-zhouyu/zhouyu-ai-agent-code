package com.zhouyu;

import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.InMemoryChatMemoryRepository;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.ai.chat.memory.repository.jdbc.JdbcChatMemoryRepository;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

/**
 * 作者：IT周瑜
 * 公众号：IT周瑜
 * 微信号：it_zhouyu
 */
@SpringBootApplication
public class ConsultationApplication {

    @Bean
    public ChatMemory chatMemory(JdbcChatMemoryRepository chatMemoryRepository) {
        return MessageWindowChatMemory.builder()
                .chatMemoryRepository(chatMemoryRepository)
                .build();
    }

    public static void main(String[] args) {
        SpringApplication.run(ConsultationApplication.class, args);
    }
}
