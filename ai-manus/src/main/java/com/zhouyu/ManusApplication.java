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
                1. 创建一个名为'test_page.html'的HTML文件并添加内容
                2. 使用file://协议在浏览器中打开本地文件
                3. 给打开的页面截图
                4. 告诉截图中的内容
                """;
        manusAgent.run(prompt);


        System.exit(0);
    }
}
