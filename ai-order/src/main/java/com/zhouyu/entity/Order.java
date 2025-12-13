package com.zhouyu.entity;

import com.zhouyu.enums.OrderStatus;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class Order {
    
    private Long orderId;
    private String userId;
    private String orderNo;
    private BigDecimal totalAmount;
    private OrderStatus status;
    
    private String remark;
    
    private String deliveryAddress;
    
    private String receiverName;
    
    private String receiverPhone;
    
    private LocalDateTime createTime;
    
    private LocalDateTime updateTime;
    
    private LocalDateTime payTime;
    
    private LocalDateTime shipTime;
    
    private LocalDateTime deliverTime;
    
    private List<OrderItem> orderItems;

    public Order() {
        this.createTime = LocalDateTime.now();
        this.updateTime = LocalDateTime.now();
        this.status = OrderStatus.PENDING;
    }

    public void setStatus(OrderStatus status) {
        this.status = status;
        this.updateTime = LocalDateTime.now();
    }
}