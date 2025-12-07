package com.zhouyu;

import com.alibaba.cloud.ai.graph.CompiledGraph;
import com.alibaba.cloud.ai.graph.GraphRepresentation;
import com.alibaba.cloud.ai.graph.agent.ReactAgent;
import com.zhouyu.tools.DateTool;
import com.zhouyu.tools.ZhouyuTools;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.support.ToolCallbacks;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.function.FunctionToolCallback;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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

    public static void main(String[] args) {
        SpringApplication.run(AgentApplication.class, args);
    }
}
