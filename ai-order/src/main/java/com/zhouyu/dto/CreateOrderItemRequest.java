package com.zhouyu.dto;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import lombok.Data;

@Data
public class CreateOrderItemRequest {
    @JsonPropertyDescription("商品ID")
    private Long productId;

    @JsonPropertyDescription("购买数量")
    private Integer quantity;
}