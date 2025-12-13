package com.zhouyu.entity;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class OrderItem {
    
    private Long itemId;
    
    private Long orderId;

    private Long productId;

    private String productName;

    private BigDecimal productPrice;

    private Integer quantity;

    private BigDecimal subtotal;
    
    private String productImage;
    
    private String productSpecs;

    public OrderItem() {}

    public OrderItem(Long productId, String productName, BigDecimal productPrice, Integer quantity) {
        this.productId = productId;
        this.productName = productName;
        this.productPrice = productPrice;
        this.quantity = quantity;
        this.subtotal = productPrice.multiply(BigDecimal.valueOf(quantity));
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
        if (this.productPrice != null && quantity != null) {
            this.subtotal = this.productPrice.multiply(BigDecimal.valueOf(quantity));
        }
    }
}