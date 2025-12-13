package com.zhouyu.dto;

import com.zhouyu.entity.Order;
import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * 作者：IT周瑜
 * 公众号：IT周瑜
 * 微信号：it_zhouyu
 */
@Data
@AllArgsConstructor
public class PaymentResponse {
    private boolean success;
    private String message;
    private Order order;
}
