package com.zhouyu.tool;

import com.zhouyu.dto.CreateOrderRequest;
import com.zhouyu.dto.PaymentResponse;
import com.zhouyu.entity.Order;
import com.zhouyu.entity.Product;
import com.zhouyu.enums.SessionStatus;
import com.zhouyu.service.OrderService;
import lombok.extern.java.Log;
import lombok.extern.log4j.Log4j2;
import org.springframework.ai.chat.model.ToolContext;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 作者：IT周瑜
 * 公众号：IT周瑜
 * 微信号：it_zhouyu
 */
@Component
@Log4j2
public class OrderTool {

    @Autowired
    private OrderService orderService;

    private final Map<String, SessionStatus> sessionContext = new ConcurrentHashMap<>();

    @Tool(description = "推荐商品信息，从商品库中根据用户需求匹配商品信息")
    public List<Product> searchProducts(@ToolParam(description = "用户原始问题") String question,  @ToolParam(description = "用户问题的关键词") String keyword) {
        log.info("推荐商品信息工具被调用");
        return orderService.searchProducts(question, keyword);
    }

    @Tool(description = "创建订单")
    public Order createOrder(CreateOrderRequest createOrderRequest) {
        log.info("创建订单工具被调用");
        return orderService.createOrder(createOrderRequest);
    }

    @Tool(description = "支付订单")
    public PaymentResponse payOrder(@ToolParam(description = "订单号") String orderNo, ToolContext toolContext) {
        log.info("支付订单工具被调用");
        try {
            String chatId = (String) toolContext.getContext().get("chatId");
            SessionStatus sessionStatus = sessionContext.get(chatId);

            if (sessionStatus == null || !sessionStatus.equals(SessionStatus.CONFIRMING_PAYMENT)) {
                return new PaymentResponse(false, "请先确认是否支付", null);
            }

            Order order = orderService.payOrder(orderNo);
            return new PaymentResponse(true, "支付成功", order);
        } catch (Exception e) {
           return new PaymentResponse(false, "支付失败: " + e.getMessage(), null);
        }
    }

    @Tool(description = "订单退款")
    public PaymentResponse refundOrder(String orderNo, @ToolParam(description = "退款原因") String reason) {
        log.info("订单退款工具被调用");
        try {
            Order order = orderService.refundOrder(orderNo, reason);
            return new PaymentResponse(true, "订单退款成功", order);
        } catch (Exception e) {
            return new PaymentResponse(false, "订单退款失败: " + e.getMessage(), null);
        }
    }

    public Map<String, SessionStatus> getSessionContext() {
        return sessionContext;
    }
}
