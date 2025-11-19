package com.zhouyu.model;

import java.util.List;

/**
 * 作者：IT周瑜
 * 公众号：IT周瑜
 * 微信号：it_zhouyu
 */
public interface RelevanceFilter {

    List<Message> filter(List<Message> messages, String currentQuery, int maxMessages);

    // 关键字 我喜欢编程，我不喜欢编程，我喜欢敲代码
    // 语义相似度 向量模型
    // 大语言模型
    double calculateRelevance(Message message, String currentQuery);
}