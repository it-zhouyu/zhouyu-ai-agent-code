package com.zhouyu;

import org.elasticsearch.client.RestClient;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.InMemoryChatMemoryRepository;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.elasticsearch.ElasticsearchVectorStore;
import org.springframework.ai.vectorstore.elasticsearch.ElasticsearchVectorStoreOptions;
import org.springframework.ai.vectorstore.elasticsearch.SimilarityFunction;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class OrderApplication {
    @Bean
    public ChatMemory chatMemory() {
        InMemoryChatMemoryRepository chatMemoryRepository = new InMemoryChatMemoryRepository();
        return MessageWindowChatMemory.builder()
                .chatMemoryRepository(chatMemoryRepository)
                .build();
    }

    @Bean
    public ElasticsearchVectorStore productVectorStore(RestClient restClient, EmbeddingModel embeddingModel) {
        ElasticsearchVectorStoreOptions options = new ElasticsearchVectorStoreOptions();
        options.setIndexName("ai-order-product-index");    // Optional: defaults to "spring-ai-document-index"
        options.setSimilarity(SimilarityFunction.cosine);           // Optional: defaults to COSINE
        options.setDimensions(1024);             // Optional: defaults to model dimensions or 1536

        return ElasticsearchVectorStore.builder(restClient, embeddingModel)
                .options(options)                     // Optional: use custom options
                .initializeSchema(true)               // Optional: defaults to false
                .build();
    }

    @Bean
    public ElasticsearchVectorStore customerVectorStore(RestClient restClient, EmbeddingModel embeddingModel) {
        ElasticsearchVectorStoreOptions options = new ElasticsearchVectorStoreOptions();
        options.setIndexName("ai-order-customer-index");    // Optional: defaults to "spring-ai-document-index"
        options.setSimilarity(SimilarityFunction.cosine);           // Optional: defaults to COSINE
        options.setDimensions(1024);             // Optional: defaults to model dimensions or 1536

        return ElasticsearchVectorStore.builder(restClient, embeddingModel)
                .options(options)                     // Optional: use custom options
                .initializeSchema(true)               // Optional: defaults to false
                .build();
    }

    public static void main(String[] args) {
        SpringApplication.run(OrderApplication.class, args);
    }
}