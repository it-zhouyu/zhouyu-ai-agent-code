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
                搜索阿里巴巴最新股价，并写入到文件中。
                """;
        manusAgent.run(prompt);


        System.exit(0);
    }
}
