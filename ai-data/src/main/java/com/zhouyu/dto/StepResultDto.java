package com.zhouyu.dto;

import lombok.Data;

/**
 * 作者：IT周瑜
 * 公众号：IT周瑜
 * 微信号：it_zhouyu
 */
@Data
public class StepResultDto {
    private Step step;
    private boolean success;
    private String data;
}
