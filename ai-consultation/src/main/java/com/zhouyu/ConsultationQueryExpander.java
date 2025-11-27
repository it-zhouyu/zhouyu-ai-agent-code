package com.zhouyu;

import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.MessageType;
import org.springframework.ai.rag.Query;
import org.springframework.ai.rag.preretrieval.query.expansion.QueryExpander;

import java.util.List;

/**
 * 作者：IT周瑜
 * 公众号：IT周瑜
 * 微信号：it_zhouyu
 */
public class ConsultationQueryExpander implements QueryExpander {
    @Override
    public List<Query> expand(Query query) {
        List<Message> history = query.history();

        return history
                .stream()
                .filter(message -> message.getMessageType().equals(MessageType.USER))
                .map(message -> Query.builder().text(message.getText()).build())
                .toList();
    }
}
