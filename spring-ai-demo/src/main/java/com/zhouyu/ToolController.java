package com.zhouyu;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.model.tool.ToolCallingChatOptions;
import org.springframework.ai.model.tool.ToolCallingManager;
import org.springframework.ai.model.tool.ToolExecutionResult;
import org.springframework.ai.support.ToolCallbacks;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

/**
 * 作者：IT周瑜
 * 公众号：IT周瑜
 * 微信号：it_zhouyu
 */
@RestController
public class ToolController {

    @Autowired
    private ChatClient chatClient;

    @Autowired
    private ZhouyuTools zhouyuTools;

    /**
     * 执行工具
     */
    @GetMapping("/tool")
    public String tool(String question) {
        return chatClient
                .prompt()
                .user(question)
                .tools(zhouyuTools)
                .call()
                .content();
    }

    // question+工具定义---》toolcall -->执行工具--》工具结果---》发送给大模型


    @GetMapping("/userControlledTool")
    public String userControlledTool(String question) {

        ToolCallback[] zhouyuTools = ToolCallbacks.from(new ZhouyuTools());

        ToolCallingChatOptions toolCallingChatOptions = ToolCallingChatOptions.builder()
                .toolCallbacks(zhouyuTools)
                .internalToolExecutionEnabled(false)
                .build();

        Prompt prompt = Prompt.builder().chatOptions(toolCallingChatOptions).content(question).build();

        ChatResponse chatResponse = chatClient.prompt(prompt).call().chatResponse();



        ToolCallingManager toolCallingManager = ToolCallingManager.builder().build();
        while (chatResponse.hasToolCalls()) {
            ToolExecutionResult toolExecutionResult = toolCallingManager.executeToolCalls(prompt, chatResponse);
            chatResponse = chatClient.prompt(new Prompt(toolExecutionResult.conversationHistory(), toolCallingChatOptions)).call().chatResponse();
        }
        return chatResponse.getResult().getOutput().getText();
    }

    @GetMapping(value = "/streamTool", produces = "text/html;charset=UTF-8")
    public Flux<String> stream(String question) {
        // 数据流
        return chatClient.prompt(question).tools(new ZhouyuTools()).stream().content();
    }
}
