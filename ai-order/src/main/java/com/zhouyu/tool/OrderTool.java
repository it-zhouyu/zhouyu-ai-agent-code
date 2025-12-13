package com.zhouyu.tool;

import com.zhouyu.dto.CreateOrderRequest;
import com.zhouyu.entity.Order;
import com.zhouyu.entity.Product;
import com.zhouyu.service.OrderService;
import lombok.extern.java.Log;
import lombok.extern.log4j.Log4j2;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

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
}
