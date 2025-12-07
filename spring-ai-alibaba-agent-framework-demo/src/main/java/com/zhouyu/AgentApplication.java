package com.zhouyu;

import com.alibaba.cloud.ai.graph.CompiledGraph;
import com.alibaba.cloud.ai.graph.GraphRepresentation;
import com.alibaba.cloud.ai.graph.agent.ReactAgent;
import com.alibaba.cloud.ai.graph.agent.hook.Hook;
import com.alibaba.cloud.ai.graph.agent.hook.hip.HumanInTheLoopHook;
import com.alibaba.cloud.ai.graph.agent.hook.hip.ToolConfig;
import com.alibaba.cloud.ai.graph.agent.hook.modelcalllimit.ModelCallLimitHook;
import com.alibaba.cloud.ai.graph.agent.hook.shelltool.ShellToolAgentHook;
import com.alibaba.cloud.ai.graph.agent.tools.ShellTool;
import com.alibaba.cloud.ai.graph.checkpoint.savers.MemorySaver;
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

        ToolCallback toolCallback = ShellTool.builder("shellWorkspace")
                .withName("shell")
                .build();

        ReactAgent agent = ReactAgent.builder()
                .name("defaultHookAgent")
                .model(chatModel)
                .tools(toolCallback)
                .hooks(ModelCallLimitHook.builder().threadLimit(1).exitBehavior(ModelCallLimitHook.ExitBehavior.ERROR).build())
                .build();
        return agent;
    }

    public static void main(String[] args) {
        SpringApplication.run(AgentApplication.class, args);
    }
}
