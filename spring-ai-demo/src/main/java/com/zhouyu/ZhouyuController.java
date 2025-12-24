package com.zhouyu;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Metrics;
import io.micrometer.observation.Observation;
import io.micrometer.observation.ObservationRegistry;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.ChatClientRequest;
import org.springframework.ai.chat.client.ChatClientResponse;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.api.CallAdvisor;
import org.springframework.ai.chat.client.advisor.api.CallAdvisorChain;
import org.springframework.ai.chat.evaluation.RelevancyEvaluator;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.ChatMemoryRepository;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.converter.BeanOutputConverter;
import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.evaluation.EvaluationRequest;
import org.springframework.ai.evaluation.EvaluationResponse;
import org.springframework.ai.rag.advisor.RetrievalAugmentationAdvisor;
import org.springframework.ai.rag.generation.augmentation.ContextualQueryAugmenter;
import org.springframework.ai.rag.generation.augmentation.QueryAugmenter;
import org.springframework.ai.rag.preretrieval.query.expansion.MultiQueryExpander;
import org.springframework.ai.rag.preretrieval.query.transformation.CompressionQueryTransformer;
import org.springframework.ai.rag.retrieval.join.ConcatenationDocumentJoiner;
import org.springframework.ai.rag.retrieval.join.DocumentJoiner;
import org.springframework.ai.rag.retrieval.search.DocumentRetriever;
import org.springframework.ai.rag.retrieval.search.VectorStoreDocumentRetriever;
import org.springframework.ai.reader.TextReader;
import org.springframework.ai.transformer.splitter.TextSplitter;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
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
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * 作者：IT周瑜
 * 公众号：IT周瑜
 * 微信号：it_zhouyu
 */
@RestController
public class ZhouyuController {

    @Autowired
    private ChatClient chatClient;

    @Autowired
    private EmbeddingModel embeddingModel;

    @Autowired
    private ChatMemory chatMemory;

    @Autowired
    private VectorStore vectorStore;

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

    @GetMapping("/memory")
    public String memory(@RequestParam("chatId") String chatId, @RequestParam("question") String question) {
        return chatClient
                .prompt()
                .advisors(MessageChatMemoryAdvisor.builder(chatMemory).build())
                .advisors(advisorSpec -> advisorSpec.params(Map.of(ChatMemory.CONVERSATION_ID, chatId)))
                .user(question)
                .call()
                .content();
    }

    @GetMapping("/advisor")
    public String advisor(String question) {
        return this.chatClient
                .prompt()
                .advisors(new ZhouyuCallAroundAdvisor())
                .user(question)
                .call()
                .content();
    }


    @GetMapping("/output")
    public Poem output(String topic) {

        BeanOutputConverter<Poem> outputConverter = new BeanOutputConverter<>(new ParameterizedTypeReference<Poem>() {
        });

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

    @GetMapping("/embedding")
    public float[] embedding(String question) {
        return embeddingModel.embed(question);
    }

    @Value("classpath:qa.txt")
    private org.springframework.core.io.Resource resource;

    @GetMapping("/store")
    public List<Document> store() {
        TextReader textReader = new TextReader(resource);
        List<Document> documents = textReader.get();

        ZhouyuTextSplitter zhouyuTextSplitter = new ZhouyuTextSplitter();
        List<Document> list = zhouyuTextSplitter.apply(documents);

//        for (Document document : list) {
//            document.getMetadata().put("author", "zhouyu");
//            document.getMetadata().put("article_type", "blog");
//        }

        vectorStore.add(list);

        return list;
    }

    @GetMapping("/search")
    public List<Document> search(String question) {
        SearchRequest searchRequest = SearchRequest
                .builder()
                .query(question)
                .topK(2)
                .similarityThreshold(0.6)
//                .filterExpression("author in ['zhouyu', 'jill'] && 'article_type' == 'blog'")
                .build();
        return vectorStore.similaritySearch(searchRequest);
    }

    @GetMapping("/ragChat")
    public String ragChat(String question) {

        // 向量搜索
        List<Document> documentList = search(question);

        // 提示词模板
        PromptTemplate promptTemplate = new PromptTemplate("{question}\n\n 用以下信息回答问题:\n {contents}");

        // 组装提示词
        Prompt prompt = promptTemplate.create(Map.of("question", question, "contents", documentList));

        // 调用大模型
        return chatClient.prompt(prompt).call().content();
    }

    @GetMapping("/ragAdvisor")
    public String ragAdvisor(String question) {

        // vectorStore 向量
        // chatMemory  聊天记录

        VectorStoreDocumentRetriever documentRetriever = VectorStoreDocumentRetriever.builder()
                .similarityThreshold(0.50)
                .vectorStore(vectorStore)
                .topK(3)
                .build();

        RetrievalAugmentationAdvisor retrievalAugmentationAdvisor = RetrievalAugmentationAdvisor.builder()
                .documentRetriever(documentRetriever)
                .build();

        return chatClient.prompt()
                .advisors(retrievalAugmentationAdvisor)
                .user(question)
                .call()
                .content();
    }

    @Autowired
    private ChatClient.Builder chatClientBuilder;

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

        RetrievalAugmentationAdvisor retrievalAugmentationAdvisor = RetrievalAugmentationAdvisor.builder()
                .queryTransformers(queryTransformer)
                .queryExpander(queryExpander)
                .documentRetriever(documentRetriever)
                .documentJoiner(documentJoiner)
                .queryAugmenter(queryAugmenter)
                .build();

        return chatClient
                .prompt()
                .advisors(MessageChatMemoryAdvisor.builder(chatMemory).build(), retrievalAugmentationAdvisor)
                .advisors(a -> a.param(ChatMemory.CONVERSATION_ID, chatId))
                .user(question)
                .call()
                .content();
    }

    @GetMapping("/evaluation")
    public EvaluationResponse evaluation(String question) {
        // RAG
        SearchRequest searchRequest = SearchRequest
                .builder()
                .query(question)
                .topK(1)
                .similarityThreshold(0.1)
                .build();
        List<Document> documents = vectorStore.similaritySearch(searchRequest);
        PromptTemplate promptTemplate = new PromptTemplate("{question}\n\n 用以下信息回答问题:\n {contents}");
        String prompt = promptTemplate.render(Map.of("question", question, "contents", documents));
//        String ragResult = chatClient.prompt(prompt).call().content();

         String ragResult = "我是周瑜";

        // 评估器（可以换成另外一个模型）, 评估是否产生了幻觉
        RelevancyEvaluator relevancyEvaluator = new RelevancyEvaluator(chatClientBuilder);
        EvaluationRequest evaluationRequest = new EvaluationRequest(question, documents, ragResult);
        return relevancyEvaluator.evaluate(evaluationRequest);
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

    static class ZhouyuCallAroundAdvisor implements CallAdvisor {
        @Override
        public ChatClientResponse adviseCall(ChatClientRequest chatClientRequest, CallAdvisorChain callAdvisorChain) {

            System.out.println("before...");

            // 增强提示词
            Prompt prompt = chatClientRequest.prompt().augmentSystemMessage("我是周瑜");
            ChatClientRequest chatClientRequestCopy = chatClientRequest.mutate().prompt(prompt).build();

            ChatClientResponse advisedResponse = callAdvisorChain.nextCall(chatClientRequestCopy);

            System.out.println("after...");

            return advisedResponse;
        }

        @Override
        public String getName() {
            return this.getClass().getSimpleName();
        }

        @Override
        public int getOrder() {
            // 数字越小，越先执行，升序排序
            return 0;
        }
    }

    static class ZhouyuTextSplitter extends TextSplitter {
        @Override
        protected List<String> splitText(String text) {
            return List.of(split(text));
        }

        public String[] split(String text) {
            return text.split("\\s*\\R\\s*\\R\\s*");
        }
    }

    // -javaagent:/Users/dadudu/dev/skywalking-agent/skywalking-agent.jar
    //-Dskywalking.agent.service_name=spring-ai-demo
    //-Dskywalking.collector.backend_service=127.0.0.1:11800

    @Autowired
    private ObservationRegistry observationRegistry;

    @Autowired
    private MeterRegistry meterRegistry;

    @GetMapping("/testMetric")
    public String testMetric() {

        meterRegistry.counter("metric.zhouyu.count", "endpoint", "/testMetric").increment();

        Observation.createNotStarted("zhouyu.business.process", observationRegistry)
                .lowCardinalityKeyValue("zhouyu.uid", "123")
                .lowCardinalityKeyValue("zhouyu.modelName", "qwen3")
                .highCardinalityKeyValue("zhouyu.trace.id", UUID.randomUUID().toString())
                .contextualName("处理业务指标")
                .observe(() -> {
                    System.out.println("业务逻辑");
                    return "success";
                });

        return "metric";
    }
}
