package com.zhouyu;

import com.alibaba.cloud.ai.graph.agent.ReactAgent;
import com.alibaba.cloud.ai.graph.exception.GraphRunnerException;
import com.zhouyu.tools.ZhouyuTools;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * 作者：IT周瑜
 * 公众号：IT周瑜
 * 微信号：it_zhouyu
 */
@RestController
public class AgentController {

    @Autowired
    private ChatClient.Builder chatClientBuilder;

    @Autowired
    private ReactAgent helloAgent;

    @GetMapping("/hello")
    public String hello(String input) {
        try {
            AssistantMessage message = helloAgent.call(input);
            return message.getText();
        } catch (GraphRunnerException e) {
            throw new RuntimeException(e);
        }
    }


    @GetMapping("/toolContext")
    public String toolContext(String input) {
        ChatClient chatClient = chatClientBuilder.build();

        return chatClient.prompt()
                .user(input)
                .tools(new ZhouyuTools())
                .toolContext(Map.of("input", input))
                .call()
                .content();
    }

}
