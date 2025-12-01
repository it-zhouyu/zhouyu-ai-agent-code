package com.zhouyu.blog;

import com.alibaba.cloud.ai.graph.OverAllState;
import com.alibaba.cloud.ai.graph.action.NodeAction;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatResponse;
import reactor.core.publisher.Flux;

import java.util.Map;

/**
 * 作者：IT周瑜
 * 公众号：IT周瑜
 * 微信号：it_zhouyu
 */
public class TitleNodeAction implements NodeAction {

    private ChatClient chatClient;

    public TitleNodeAction(ChatClient chatClient) {
        this.chatClient = chatClient;
    }

    @Override
    public Map<String, Object> apply(OverAllState state) throws Exception {
        // 获取输入
        String subject = state.value("subject", String.class).orElseThrow();

        Flux<ChatResponse> title = chatClient.prompt()
                .system("给指定主题生成一个爆款文章标题")
                .user("主题：" + subject)
                .stream()
                .chatResponse();

        return Map.of("title", title);
    }
}
