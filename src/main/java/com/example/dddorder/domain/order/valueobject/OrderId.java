package com.example.dddorder.domain.order.valueobject;

// 订单ID值对象，用 record 保证不可变性，equals/hashCode 自动生成
public record OrderId(Long value) {

    // 构造时校验，ID 不允许为空或负数
    public OrderId {
        if (value == null || value <= 0) {
            throw new IllegalArgumentException("OrderId must be positive");
        }
    }
}
