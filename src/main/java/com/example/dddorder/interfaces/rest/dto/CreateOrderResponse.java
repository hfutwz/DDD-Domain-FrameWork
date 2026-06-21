package com.example.dddorder.interfaces.rest.dto;

// 出参 DTO：封装返回给客户端的响应数据
public record CreateOrderResponse(
        Long orderId,   // 创建成功后的订单ID
        String message  // 提示信息
) {}
