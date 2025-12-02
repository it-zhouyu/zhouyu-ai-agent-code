package com.zhouyu.dto;

import lombok.Data;

/**
 * 作者：IT周瑜
 * 公众号：IT周瑜
 * 微信号：it_zhouyu
 */
@Data
public class Step {
    private String title;
    private String prompt;  // 发送给大模型进行调研
}
