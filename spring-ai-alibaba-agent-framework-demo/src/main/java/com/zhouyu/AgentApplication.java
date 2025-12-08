package com.zhouyu;

import com.alibaba.cloud.ai.graph.CompiledGraph;
import com.alibaba.cloud.ai.graph.GraphRepresentation;
import com.alibaba.cloud.ai.graph.agent.ReactAgent;
import com.alibaba.cloud.ai.graph.agent.extension.interceptor.FilesystemInterceptor;
import com.alibaba.cloud.ai.graph.agent.flow.agent.LlmRoutingAgent;
import com.alibaba.cloud.ai.graph.agent.flow.agent.ParallelAgent;
import com.alibaba.cloud.ai.graph.agent.flow.agent.SequentialAgent;
import com.alibaba.cloud.ai.graph.agent.hook.Hook;
import com.alibaba.cloud.ai.graph.agent.hook.hip.HumanInTheLoopHook;
import com.alibaba.cloud.ai.graph.agent.hook.hip.ToolConfig;
import com.alibaba.cloud.ai.graph.agent.hook.modelcalllimit.ModelCallLimitHook;
import com.alibaba.cloud.ai.graph.agent.hook.pii.PIIDetectionHook;
import com.alibaba.cloud.ai.graph.agent.hook.pii.PIIType;
import com.alibaba.cloud.ai.graph.agent.hook.pii.RedactionStrategy;
import com.alibaba.cloud.ai.graph.agent.hook.shelltool.ShellToolAgentHook;
import com.alibaba.cloud.ai.graph.agent.hook.summarization.SummarizationHook;
import com.alibaba.cloud.ai.graph.agent.interceptor.contextediting.ContextEditingInterceptor;
import com.alibaba.cloud.ai.graph.agent.interceptor.todolist.TodoListInterceptor;
import com.alibaba.cloud.ai.graph.agent.interceptor.toolselection.ToolSelectionInterceptor;
import com.alibaba.cloud.ai.graph.agent.tools.ShellTool;
import com.alibaba.cloud.ai.graph.checkpoint.savers.MemorySaver;
import com.alibaba.cloud.ai.graph.exception.GraphStateException;
import com.alibaba.cloud.ai.graph.store.StoreItem;
import com.alibaba.cloud.ai.graph.store.stores.MemoryStore;
import com.zhouyu.hooks.LoggingHook;
import com.zhouyu.hooks.ZhouyuModelHook;
import com.zhouyu.interceptor.ZhouyuModelInterceptor;
import com.zhouyu.interceptor.ZhouyuToolInterceptor;
import com.zhouyu.tools.DateTool;
import com.zhouyu.tools.StoreTool;
import com.zhouyu.tools.ZhouyuTools;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.support.ToolCallbacks;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.function.FunctionToolCallback;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.util.*;

/**
 * 作者：IT周瑜
 * 公众号：IT周瑜
 * 微信号：it_zhouyu
 */
@SpringBootApplication
public class AgentApplication {

    @Bean
    public ReactAgent helloAgent(ChatModel chatModel) {

        ToolCallback dateTool = FunctionToolCallback
                .builder("getDate", new DateTool())
                .description("获取指定城市的当前时间")
                .inputType(DateTool.DateRequest.class)
                .build();

        ToolCallback[] toolCallbacks = ToolCallbacks.from(new ZhouyuTools());

        List<ToolCallback> list = new ArrayList<>(Arrays.asList(toolCallbacks));
        list.add(dateTool);

        ReactAgent reactAgent = ReactAgent.builder()
                .name("helloAgent")
                .model(chatModel)
                .systemPrompt("简短的回答用户问题")
                .tools(list)
                .build();

        CompiledGraph compiledGraph = reactAgent.getAndCompileGraph();
        GraphRepresentation representation = compiledGraph.getGraph(GraphRepresentation.Type.MERMAID);
        System.out.println(representation.content());

        return reactAgent;
    }

    @Bean
    public MemorySaver memorySaver() {
        return new MemorySaver();
    }

    @Bean
    public ReactAgent memoryAgent(ChatModel chatModel, MemorySaver memorySaver) {


        // MemorySaver ChatClient ChatMemory

        ReactAgent reactAgent = ReactAgent.builder()
                .name("memoryAgent")
                .model(chatModel)
                .systemPrompt("简短的回答用户问题")
                .saver(memorySaver)
                .build();

        return reactAgent;
    }

    @Bean
    public ReactAgent hookAgent(ChatModel chatModel) {
        ReactAgent reactAgent = ReactAgent.builder()
                .name("hookAgent")
                .model(chatModel)
                .tools(ToolCallbacks.from(new ZhouyuTools()))
                .systemPrompt("简短的回答用户问题")
                .hooks(new LoggingHook(), new ZhouyuModelHook())
                .interceptors(new ZhouyuModelInterceptor(), new ZhouyuToolInterceptor())
                .build();

        CompiledGraph compiledGraph = reactAgent.getAndCompileGraph();
        GraphRepresentation representation = compiledGraph.getGraph(GraphRepresentation.Type.MERMAID);
        System.out.println(representation.content());


        return reactAgent;
    }

    @Bean
    public ReactAgent humanHookAgent(ChatModel chatModel) {
        Hook humanInTheLoopHook = HumanInTheLoopHook.builder()
                .approvalOn("getWeather", ToolConfig.builder().description("请确认是否执行工具").build())
                .build();

        ReactAgent agent = ReactAgent.builder()
                .name("humanHookAgent")
                .model(chatModel)
                .saver(new MemorySaver())
                .tools(ToolCallbacks.from(new ZhouyuTools()))
                .hooks(humanInTheLoopHook)
                .build();
        return agent;
    }

    @Bean
    public MemoryStore memoryStore() {

        MemoryStore memoryStore = new MemoryStore();

        // 相当于mysql中的数据
        Map<String, Object> userData = new HashMap<>();
        userData.put("username", "周瑜");
        StoreItem storeItem = StoreItem.of(List.of("user_info"), "user_002", userData);
        memoryStore.putItem(storeItem);

        return memoryStore;
    }

    @Bean
    public ReactAgent storeAgent(ChatModel chatModel) {
        ToolCallback storeTool = FunctionToolCallback
                .builder("getWeather", new StoreTool())
                .description("获取指定城市的天气信息")
                .inputType(StoreTool.WeatherRequest.class)
                .build();

        ReactAgent agent = ReactAgent.builder()
                .name("storeAgent")
                .model(chatModel)
                .saver(new MemorySaver())
                .tools(storeTool)
                .build();
        return agent;
    }

    @Bean
    public ReactAgent defaultHookAgent(ChatModel chatModel) {

        ToolSelectionInterceptor interceptor = ToolSelectionInterceptor.builder()
                .selectionModel(chatModel)
                .build();

        ReactAgent agent = ReactAgent.builder()
                .name("defaultHookAgent")
                .model(chatModel)
                .interceptors(interceptor)
                .tools(ToolCallbacks.from(new ZhouyuTools()))
                .saver(new MemorySaver())
                .build();
        return agent;
    }

    @Bean
    public SequentialAgent sequentialAgent(ChatModel chatModel) throws GraphStateException {

        ReactAgent planAgent = ReactAgent.builder()
                .name("planAgent")
                .model(chatModel)
                .systemPrompt("根据用户需求制定执行计划，你只负责制定计划，不要执行计划")
                .outputKey("planResult")
                .build();

        ReactAgent executeAgent = ReactAgent.builder()
                .name("executeAgent")
                .model(chatModel)
                .systemPrompt("根据执行计划执行任务")
                .build();

        SequentialAgent sequentialAgent = SequentialAgent.builder()
                .name("sequentialAgent")
                .subAgents(List.of(planAgent, executeAgent))
                .build();

        return sequentialAgent;

    }

    @Bean
    public ParallelAgent parallelAgent(ChatModel chatModel) throws GraphStateException {

        ReactAgent javaAgent = ReactAgent.builder()
                .name("javaAgent")
                .model(chatModel)
                .systemPrompt("你是一个Java程序员")
                .outputKey("javaCode")
                .build();

        ReactAgent pythonAgent = ReactAgent.builder()
                .name("pythonAgent")
                .model(chatModel)
                .systemPrompt("你是一个Python程序员")
                .outputKey("pythonCode")
                .build();

        ParallelAgent parallelAgent = ParallelAgent.builder()
                .name("parallelAgent")
                .subAgents(List.of(javaAgent, pythonAgent))
                .mergeStrategy(new ParallelAgent.DefaultMergeStrategy())
                .mergeOutputKey("code")
                .build();

        return parallelAgent;

    }

    @Bean
    public LlmRoutingAgent llmRoutingAgent(ChatModel chatModel) throws GraphStateException {

        ReactAgent codeAgent = ReactAgent.builder()
                .name("agent1")
                .model(chatModel)
                .description("这是一个专门用来写Python代码的Agent")
                .systemPrompt("你是一个程序员，写python")
                .build();

        ReactAgent poemAgent = ReactAgent.builder()
                .name("agent2")
                .model(chatModel)
                .description("这是一个专门用来写五言绝句的Agent")
                .systemPrompt("你是一个诗人，写五言绝句")
                .build();

        LlmRoutingAgent llmRoutingAgent = LlmRoutingAgent.builder()
                .name("llmRoutingAgent")
                .model(chatModel)
                .subAgents(List.of(codeAgent, poemAgent))
                .description("根据用户的输入，进行意图识别，判断是要写代码还是写诗")
                .build();

        return llmRoutingAgent;

    }

    public static void main(String[] args) {
        SpringApplication.run(AgentApplication.class, args);
    }
}
