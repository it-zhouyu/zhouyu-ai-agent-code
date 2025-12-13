package com.zhouyu.service;

import com.zhouyu.dto.CreateOrderItemRequest;
import com.zhouyu.dto.CreateOrderRequest;
import com.zhouyu.entity.Order;
import com.zhouyu.entity.OrderItem;
import com.zhouyu.entity.Product;
import com.zhouyu.enums.OrderStatus;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

@Service
public class OrderService {

    private final Map<Long, Order> orderStorage = new ConcurrentHashMap<>();
    private final AtomicLong orderIdGenerator = new AtomicLong(1);
    private final AtomicLong itemIdGenerator = new AtomicLong(1);

    private final Map<Long, Product> productStorage = new ConcurrentHashMap<>();

    @Autowired
    private VectorStore vectorStore;

    public OrderService() {
        initMockProducts();
    }

    private void initMockProducts() {
        productStorage.put(1L, new Product(1L, "iPhone 15 Pro", new BigDecimal("7999.00"), "最新款iPhone，支持5G，A17 Pro芯片"));
        productStorage.put(2L, new Product(2L, "iPhone 15", new BigDecimal("5999.00"), "iPhone 15标准版，A16仿生芯片"));
        productStorage.put(3L, new Product(3L, "iPhone 14", new BigDecimal("4999.00"), "iPhone 14，A15仿生芯片"));
        productStorage.put(4L, new Product(4L, "MacBook Pro 14", new BigDecimal("15999.00"), "专业笔记本电脑，M3 Pro芯片"));
        productStorage.put(5L, new Product(5L, "MacBook Air", new BigDecimal("8999.00"), "轻薄笔记本电脑，M2芯片"));
        productStorage.put(6L, new Product(6L, "AirPods Pro", new BigDecimal("1999.00"), "无线蓝牙耳机，主动降噪"));
        productStorage.put(7L, new Product(7L, "iPad Air", new BigDecimal("4599.00"), "轻薄平板电脑，M1芯片"));
        productStorage.put(8L, new Product(8L, "iPad Pro", new BigDecimal("6799.00"), "专业平板电脑，M2芯片"));
        productStorage.put(9L, new Product(9L, "Apple Watch Series 9", new BigDecimal("2999.00"), "最新款智能手表"));
        productStorage.put(10L, new Product(10L, "Apple Watch SE", new BigDecimal("1999.00"), "性价比智能手表"));
    }

    public Order createOrder(CreateOrderRequest request) {
        Order order = new Order();
        order.setOrderId(orderIdGenerator.getAndIncrement());
        order.setOrderNo(generateOrderNo());
        order.setUserId(request.getUserId());
        order.setRemark(request.getRemark());
        order.setDeliveryAddress(request.getDeliveryAddress());
        order.setReceiverName(request.getReceiverName());
        order.setReceiverPhone(request.getReceiverPhone());

        List<OrderItem> orderItems = new ArrayList<>();
        BigDecimal totalAmount = BigDecimal.ZERO;

        for (CreateOrderItemRequest itemRequest : request.getItems()) {
            Product product = productStorage.get(itemRequest.getProductId());
            if (product == null) {
                throw new RuntimeException("商品不存在: " + itemRequest.getProductId());
            }

            OrderItem item = new OrderItem();
            item.setItemId(itemIdGenerator.getAndIncrement());
            item.setOrderId(order.getOrderId());
            item.setProductId(product.getId());
            item.setProductName(product.getName());
            item.setProductPrice(product.getPrice());
            item.setQuantity(itemRequest.getQuantity());
            item.setSubtotal(product.getPrice().multiply(BigDecimal.valueOf(itemRequest.getQuantity())));

            orderItems.add(item);
            totalAmount = totalAmount.add(item.getSubtotal());
        }

        order.setOrderItems(orderItems);
        order.setTotalAmount(totalAmount);

        orderStorage.put(order.getOrderId(), order);
        return order;
    }

    public Order getOrderByOrderNo(String orderNo) {
        return orderStorage.values().stream()
                .filter(order -> order.getOrderNo().equals(orderNo))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("订单不存在: " + orderNo));
    }

    public Order payOrder(String orderNo) {
        Order order = getOrderByOrderNo(orderNo);
        if (order.getStatus() != OrderStatus.PENDING) {
            throw new RuntimeException("订单状态不允许支付: " + order.getStatus().getDescription());
        }

        order.setStatus(OrderStatus.PAID);
        order.setPayTime(LocalDateTime.now());
        return order;
    }

    public Order refundOrder(String orderNo, String reason) {
        Order order = getOrderByOrderNo(orderNo);
        if (order.getStatus() == OrderStatus.PENDING || order.getStatus() == OrderStatus.CANCELLED) {
            throw new RuntimeException("订单状态不允许退款: " + order.getStatus().getDescription());
        }

        order.setStatus(OrderStatus.REFUNDED);
        order.setRemark(order.getRemark() + " [退款原因: " + reason + "]");
        return order;
    }

    public List<Order> getUserOrders(String userId) {
        return orderStorage.values().stream()
                .filter(order -> order.getUserId().equals(userId))
                .sorted((o1, o2) -> o2.getCreateTime().compareTo(o1.getCreateTime()))
                .collect(Collectors.toList());
    }

    private String generateOrderNo() {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        String random = String.valueOf((int) (Math.random() * 1000));
        return "ORD" + timestamp + String.format("%03d", Integer.parseInt(random));
    }

    public void initProductVector() {
        List<Document> documentList = productStorage.values().stream().map(product -> {
            String content = product.getName() + ": " + product.getDescription();
            Map<String, Object> metadata = new HashMap<>();
            metadata.put("productId", product.getId());
            metadata.put("productName", product.getName());
            metadata.put("description", product.getDescription());
            metadata.put("price", product.getPrice());
            return new Document(content, metadata);
        }).toList();
        vectorStore.add(documentList);
    }

    public List<Product> searchProducts(String question, String keyword) {
        List<Document> documents = vectorStore.similaritySearch(SearchRequest.builder()
                .query(question)
                .topK(5)
                .build());
        List<Product> similaritySearchResult = documents.stream()
                .map(document -> {
                    Map<String, Object> metadata = document.getMetadata();
                    Long productId = ((Integer) metadata.get("productId")).longValue();
                    return productStorage.get(productId);
                }).toList();


        String lowerKeyword = keyword.toLowerCase();
        List<Product> keywordResult = productStorage.values().stream()
                .filter(product -> product.getName().toLowerCase().contains(lowerKeyword) || product.getDescription().toLowerCase().contains(lowerKeyword))
                .toList();

        List<Product> result = new ArrayList<>();
        result.addAll(similaritySearchResult);
        result.addAll(keywordResult);

        return result;
    }
}