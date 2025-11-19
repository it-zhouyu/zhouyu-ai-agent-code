package com.zhouyu.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 作者：IT周瑜
 * 公众号：IT周瑜
 * 微信号：it_zhouyu
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ToolCall {
    private String id;
    private String type = "function";
    private Function function;
}