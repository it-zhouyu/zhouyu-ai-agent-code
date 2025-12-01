package com.zhouyu.blog;

import com.alibaba.cloud.ai.graph.OverAllState;
import com.alibaba.cloud.ai.graph.action.NodeAction;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;

import java.util.Map;

/**
 * 作者：IT周瑜
 * 公众号：IT周瑜
 * 微信号：it_zhouyu
 */
@Slf4j
public class ContentNodeAction implements NodeAction {

    private ChatClient chatClient;

    public ContentNodeAction(ChatClient chatClient) {
        this.chatClient = chatClient;
    }

    @Override
    public Map<String, Object> apply(OverAllState state) throws Exception {
        // 获取输入
        String title = state.value("title", String.class).orElseThrow();

        log.info("title: {}", title);

        String content = chatClient.prompt()
                .system("给指定主题生成一篇爆款文章, 内容要简短")
                .user("主题：" + title)
                .call()
                .content();

        return Map.of("content", content);
    }
}
