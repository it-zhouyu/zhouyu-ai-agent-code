package com.zhouyu.node;

import com.alibaba.cloud.ai.graph.OverAllState;
import com.alibaba.cloud.ai.graph.action.NodeAction;
import org.springframework.ai.chat.client.ChatClient;

import java.util.Map;

/**
 * 作者：IT周瑜
 * 公众号：IT周瑜
 * 微信号：it_zhouyu
 */
public class KeywordsExtractNode implements NodeAction {

    private ChatClient chatClient;

    public KeywordsExtractNode(ChatClient chatClient) {
        this.chatClient = chatClient;
    }

    @Override
    public Map<String, Object> apply(OverAllState state) throws Exception {
        // 定义提示词
        String prompt = """
                请将输入需求进行关键字抽取，并返回一个JSON格式的关键字列表，格式如下：
                {"keywords": ["关键字1", "关键字2", "关键字3"]}
                """;

        String input = state.value("input", String.class).orElseThrow();

        String content = chatClient.prompt().system(prompt).user(input).call().content();

        return Map.of("keywordsExtractResult", content);
    }
}
