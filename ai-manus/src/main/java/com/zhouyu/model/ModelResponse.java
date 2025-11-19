package com.zhouyu.model;
import lombok.Data;

import java.util.List;

/**
 * 作者：IT周瑜
 * 公众号：IT周瑜
 * 微信号：it_zhouyu
 */
@Data
public class ModelResponse {
    private final String content;
    private final List<Object> toolCalls;
    private final String finishReason;

    public boolean hasToolCalls() {
        return toolCalls != null && !toolCalls.isEmpty();
    }

}
