package com.zhouyu;

import com.zhouyu.agent.ManusAgent;
import com.zhouyu.model.ModelConfig;
import com.zhouyu.model.OpenAIClient;

/**
 * 作者：IT周瑜
 * 公众号：IT周瑜
 * 微信号：it_zhouyu
 */
public class ManusApplication {
    public static void main(String[] args) {
        OpenAIClient openAIClient = new OpenAIClient(ModelConfig.builder()
                .baseUrl("https://dashscope.aliyuncs.com/compatible-mode/v1")
                .apiKey(System.getenv("DASHSCOPE_API_KEY"))
                .model("qwen-plus")
                .build());

        ManusAgent manusAgent = new ManusAgent(openAIClient);

        String prompt = """
                请帮我用HTML、CSS、JS创建一个简单的贪吃蛇游戏，分成三个文件，分别是snake.html、snake.css、snake.js
                """;
        manusAgent.run(prompt);

        System.exit(0);
    }
}
