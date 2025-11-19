package com.zhouyu.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * 作者：IT周瑜
 * 公众号：IT周瑜
 * 微信号：it_zhouyu
 */
@Data
public class Memory {

    private List<Message> messages;
    private RelevanceFilter relevanceFilter;

    // 相关性过滤

    public Memory() {
        this.messages = new ArrayList<>();
    }

    public void addMessage(Message message) {
        messages.add(message);
    }

    public List<Message> getMessages(String currentQuery) {
        if (relevanceFilter != null) {
            return relevanceFilter.filter(messages, currentQuery, 5);
        }
        return messages;
    }
}
