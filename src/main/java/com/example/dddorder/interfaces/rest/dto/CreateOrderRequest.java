package com.example.dddorder.interfaces.rest.dto;

import java.math.BigDecimal;
import java.util.List;

// 入参 DTO：接收 HTTP 请求体的 JSON 数据，只做数据承载，不包含业务逻辑
public record CreateOrderRequest(
        Long userId,       // 下单用户ID
        List<Item> items   // 订单明细
) {
    // 明细 DTO，嵌套在请求体内
    public record Item(
            Long productId,       // 商品ID
            String productName,   // 商品名称
            int quantity,         // 数量
            BigDecimal unitPrice  // 单价
    ) {}
}
