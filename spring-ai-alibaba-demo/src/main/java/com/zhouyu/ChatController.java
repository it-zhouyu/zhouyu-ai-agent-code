package com.zhouyu;

import com.alibaba.cloud.ai.advisor.DashScopeDocumentAnalysisAdvisor;
import com.alibaba.cloud.ai.advisor.RetrievalRerankAdvisor;
import com.alibaba.cloud.ai.model.RerankModel;
import com.alibaba.cloud.ai.rag.postretrieval.DashScopeRerankPostProcessor;
import com.alibaba.cloud.ai.toolcalling.baidusearch.BaiduSearchService;
import com.alibaba.cloud.ai.toolcalling.time.GetTimeByZoneIdService;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.model.SimpleApiKey;
import org.springframework.ai.rag.advisor.RetrievalAugmentationAdvisor;
import org.springframework.ai.rag.generation.augmentation.ContextualQueryAugmenter;
import org.springframework.ai.rag.generation.augmentation.QueryAugmenter;
import org.springframework.ai.rag.preretrieval.query.expansion.MultiQueryExpander;
import org.springframework.ai.rag.preretrieval.query.transformation.CompressionQueryTransformer;
import org.springframework.ai.rag.retrieval.join.ConcatenationDocumentJoiner;
import org.springframework.ai.rag.retrieval.join.DocumentJoiner;
import org.springframework.ai.rag.retrieval.search.DocumentRetriever;
import org.springframework.ai.rag.retrieval.search.VectorStoreDocumentRetriever;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 作者：IT周瑜
 * 公众号：IT周瑜
 * 微信号：it_zhouyu
 */
@RestController
public class ChatController {

    @Autowired
    private ChatClient chatClient;

    @Autowired
    private ChatClient.Builder chatClientBuilder;

    @Autowired
    private ChatMemory chatMemory;

    @Autowired
    private RerankModel rerankModel;

    @Autowired
    private BaiduSearchService baiduSearchService;

    @Autowired
    private VectorStore vectorStore;

    @Value("${spring.ai.dashscope.api-key}")
    private String apiKey;

    @Value("classpath:qa.txt")
    private org.springframework.core.io.Resource resource;

    @GetMapping("/chat")
    public String chat(String question) {
        return chatClient.prompt(question)
                .toolNames("getCityTimeFunction")
                .call()
                .content();
    }

    @GetMapping("/baidu")
    public BaiduSearchService.Response baidu(String question) {
        BaiduSearchService.Response result = baiduSearchService.apply(new BaiduSearchService.Request(question, 10));
        return result;
    }

    @GetMapping("/rankChat")
    public String rankChat(String question) {
        return this.chatClient
                .prompt()
                .user(question)
                .advisors(new RetrievalRerankAdvisor(vectorStore, rerankModel))
                .call()
                .content();
    }

    @GetMapping("/ragAdvisor2")
    public String ragAdvisor2(@RequestParam("chatId") String chatId, @RequestParam("question") String question) {

        // 将用户问题和聊天记录进行压缩  大模型 Qwen
        CompressionQueryTransformer queryTransformer = CompressionQueryTransformer.builder().chatClientBuilder(chatClientBuilder).build();

        // 将用户问题扩写为多个问题
        MultiQueryExpander queryExpander = MultiQueryExpander.builder().chatClientBuilder(chatClientBuilder).build();

        // 从向量数据库中进行语义相似度查询
        DocumentRetriever documentRetriever = VectorStoreDocumentRetriever.builder()
                .vectorStore(vectorStore)
                .similarityThreshold(0.5)
                .topK(3)
                .build();

        // 对多个Document进行合并
        DocumentJoiner documentJoiner = new ConcatenationDocumentJoiner();

        // 增强查询，基于用户问题和检索结果进行增强查询
        QueryAugmenter queryAugmenter = ContextualQueryAugmenter.builder().build();

        DashScopeRerankPostProcessor dashScopeRerankPostProcessor = DashScopeRerankPostProcessor.builder().rerankModel(rerankModel).build();

        RetrievalAugmentationAdvisor retrievalAugmentationAdvisor = RetrievalAugmentationAdvisor.builder()
                .queryTransformers(queryTransformer)
                .queryExpander(queryExpander)
                .documentRetriever(documentRetriever)
                .documentJoiner(documentJoiner)
                .queryAugmenter(queryAugmenter)
                .documentPostProcessors(dashScopeRerankPostProcessor)
                .build();

        return chatClient
                .prompt()
                .advisors(MessageChatMemoryAdvisor.builder(chatMemory).build(), retrievalAugmentationAdvisor)
                .advisors(a -> a.param(ChatMemory.CONVERSATION_ID, chatId))
                .user(question)
                .call()
                .content();
    }

    @GetMapping("/fileChat")
    public String fileChat(String question) {
        return this.chatClient
                .prompt()
//                .system("请根据提供的文件内容回答问题")
                .user(question)
                .advisors(advisorSpec -> advisorSpec
                        .advisors(new DashScopeDocumentAnalysisAdvisor(new SimpleApiKey(apiKey)))
                        .param(DashScopeDocumentAnalysisAdvisor.RESOURCE, resource))
                .call()
                .content();
    }
}
