package com.zhouyu.dto;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import lombok.Data;

import java.util.List;

@Data
public class CreateOrderRequest {

    @JsonPropertyDescription("用户ID")
    private String userId;

    @JsonPropertyDescription("订单项")
    private List<CreateOrderItemRequest> items;

    @JsonPropertyDescription("订单备注")
    private String remark;

    @JsonPropertyDescription("收货地址")
    private String deliveryAddress;

    @JsonPropertyDescription("收货人姓名")
    private String receiverName;

    @JsonPropertyDescription("收货人电话")
    private String receiverPhone;
}