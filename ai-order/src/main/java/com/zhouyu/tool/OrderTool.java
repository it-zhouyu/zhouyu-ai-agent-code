package com.zhouyu.tool;

import com.zhouyu.entity.Product;
import com.zhouyu.service.OrderService;
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
public class OrderTool {

    @Autowired
    private OrderService orderService;

    @Tool(description = "推荐商品信息")
    public List<Product> searchProducts(@ToolParam(description = "用户原始问题") String question,  @ToolParam(description = "用户问题的关键词") String keyword) {
        return orderService.searchProducts(question, keyword);
    }
}
