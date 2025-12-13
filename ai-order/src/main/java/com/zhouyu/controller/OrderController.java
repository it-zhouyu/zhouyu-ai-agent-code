package com.zhouyu.controller;

import com.zhouyu.dto.*;
import com.zhouyu.entity.Order;
import com.zhouyu.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    @Autowired
    private OrderService orderService;

    @PostMapping
    public ApiResponse<Order> createOrder(@RequestBody CreateOrderRequest request) {
        try {
            Order order = orderService.createOrder(request);
            return ApiResponse.success("订单创建成功", order);
        } catch (Exception e) {
            return ApiResponse.error("订单创建失败: " + e.getMessage());
        }
    }

    @GetMapping("/orderNo/{orderNo}")
    public ApiResponse<Order> getOrderByOrderNo(@PathVariable String orderNo) {
        try {
            Order order = orderService.getOrderByOrderNo(orderNo);
            return ApiResponse.success(order);
        } catch (Exception e) {
            return ApiResponse.error("订单查询失败: " + e.getMessage());
        }
    }

    @GetMapping("/user/{userId}")
    public ApiResponse<List<Order>> getUserOrders(@PathVariable String userId) {
        try {
            List<Order> orders = orderService.getUserOrders(userId);
            return ApiResponse.success(orders);
        } catch (Exception e) {
            return ApiResponse.error("用户订单查询失败: " + e.getMessage());
        }
    }

    @PostMapping("/{orderNo}/pay")
    public ApiResponse<Order> payOrder(@PathVariable String orderNo) {
        try {
            Order order = orderService.payOrder(orderNo);
            return ApiResponse.success("订单支付成功", order);
        } catch (Exception e) {
            return ApiResponse.error("订单支付失败: " + e.getMessage());
        }
    }

    @PostMapping("/{orderNo}/refund")
    public ApiResponse<Order> refundOrder(@PathVariable String orderNo, @RequestParam(required = false) String reason) {
        try {
            Order order = orderService.refundOrder(orderNo, reason != null ? reason : "用户申请退款");
            return ApiResponse.success("订单退款成功", order);
        } catch (Exception e) {
            return ApiResponse.error("订单退款失败: " + e.getMessage());
        }
    }
}