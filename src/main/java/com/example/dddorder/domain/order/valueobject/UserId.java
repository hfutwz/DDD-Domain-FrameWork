package com.example.dddorder.domain.order.valueobject;

// 用户ID值对象，与 OrderId 分开定义，避免混用导致逻辑错误
public record UserId(Long value) {

    // 构造时校验，用户ID不允许为空或负数
    public UserId {
        if (value == null || value <= 0) {
            throw new IllegalArgumentException("UserId must be positive");
        }
    }
}
