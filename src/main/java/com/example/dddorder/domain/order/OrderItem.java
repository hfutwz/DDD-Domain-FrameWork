package com.example.dddorder.domain.order;

import com.example.dddorder.domain.order.valueobject.Money;

// 订单明细实体，属于 Order 聚合内部，不能脱离 Order 单独存在
public class OrderItem {

    // 商品ID，标识是哪个商品
    private final Long productId;

    // 商品名称，冗余存储，避免查询时关联商品表
    private final String productName;

    // 购买数量
    private final int quantity;

    // 单价
    private final Money unitPrice;

    public OrderItem(Long productId, String productName, int quantity, Money unitPrice) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be positive");
        }
        this.productId = productId;
        this.productName = productName;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
    }

    // 计算该明细的小计金额 = 单价 × 数量
    public Money subtotal() {
        return Money.of(unitPrice.amount().multiply(java.math.BigDecimal.valueOf(quantity)));
    }

    public Long getProductId() { return productId; }
    public String getProductName() { return productName; }
    public int getQuantity() { return quantity; }
    public Money getUnitPrice() { return unitPrice; }
}
