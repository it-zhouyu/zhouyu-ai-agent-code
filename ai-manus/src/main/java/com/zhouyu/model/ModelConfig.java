package com.zhouyu.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
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
@Builder
public class ModelConfig {
    private String model;
    private String baseUrl;
    private String apiKey;
}