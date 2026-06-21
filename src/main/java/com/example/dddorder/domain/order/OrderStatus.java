package com.example.dddorder.domain.order;

// 订单状态枚举，领域层定义，代表订单的生命周期阶段
public enum OrderStatus {
    CREATED,    // 已创建，初始状态
    PAID,       // 已支付
    CANCELLED   // 已取消
}
