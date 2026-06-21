package com.example.dddorder.domain.order.valueobject;

import java.math.BigDecimal;

// 金额值对象，封装货币计算逻辑，防止直接用 BigDecimal 散落在各处
public record Money(BigDecimal amount) {

    // 构造时校验，金额不允许为 null 或负数
    public Money {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Money amount must be non-negative");
        }
    }

    // 工厂方法，方便用 long/double 创建
    public static Money of(BigDecimal amount) {
        return new Money(amount);
    }

    // 金额相加，返回新的 Money 对象（值对象不可变）
    public Money add(Money other) {
        return new Money(this.amount.add(other.amount));
    }

    // 判断金额是否大于零，用于业务规则校验
    public boolean isPositive() {
        return this.amount.compareTo(BigDecimal.ZERO) > 0;
    }
}
