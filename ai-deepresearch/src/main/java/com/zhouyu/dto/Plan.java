package com.zhouyu.dto;

import lombok.Data;

import java.util.List;

/**
 * 作者：IT周瑜
 * 公众号：IT周瑜
 * 微信号：it_zhouyu
 */
@Data
public class Plan {

    private String title;
    private List<Step> steps;
}
