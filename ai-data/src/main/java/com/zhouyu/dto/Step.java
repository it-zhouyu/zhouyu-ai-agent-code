package com.zhouyu.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

/**
 * 作者：IT周瑜
 * 公众号：IT周瑜
 * 微信号：it_zhouyu
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class Step {
    private Integer stepNum;
    private String description;
    private String sql;  // 需要执行的sql
}
