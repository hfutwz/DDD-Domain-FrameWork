package com.example.dddorder.domain.order;

import com.example.dddorder.domain.order.valueobject.Money;
import com.example.dddorder.domain.order.valueobject.OrderId;
import com.example.dddorder.domain.order.valueobject.UserId;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

// 订单聚合根，是整个订单领域的入口，所有对订单的操作都通过这个类
// 注意：这是纯 Java 类，没有任何 Spring、JPA 注解，与框架完全解耦
public class Order {

    // 订单ID，聚合根标识
    private OrderId id;

    // 下单用户ID
    private final UserId userId;

    // 订单明细列表，外部只能读，不能直接修改（通过方法控制）
    private final List<OrderItem> items = new ArrayList<>();

    // 订单总金额，由 items 汇总计算，不允许外部直接设置
    private Money totalAmount;

    // 订单状态，初始为 CREATED
    private OrderStatus status;

    // 创建时间
    private final LocalDateTime createdAt;

    // 构造方法私有，强制通过工厂方法创建，保证业务规则一定被执行
    private Order(UserId userId) {
        this.userId = userId;
        this.status = OrderStatus.CREATED; // 初始状态固定为 CREATED
        this.createdAt = LocalDateTime.now();
        this.totalAmount = Money.of(BigDecimal.ZERO);
    }

    // 工厂方法：创建一个新订单，传入用户ID和明细列表
    public static Order create(UserId userId, List<OrderItem> items) {
        Order order = new Order(userId);
        items.forEach(order::addItem);
        order.validateTotalAmount(); // 业务规则：总金额必须大于0
        return order;
    }

    // 添加订单明细，并重新计算总金额
    private void addItem(OrderItem item) {
        this.items.add(item);
        this.totalAmount = this.totalAmount.add(item.subtotal()); // 总金额 = 各明细小计累加
    }

    // 业务规则：总金额必须大于0，否则订单不合法
    private void validateTotalAmount() {
        if (!this.totalAmount.isPositive()) {
            throw new IllegalStateException("Order total amount must be greater than zero");
        }
    }

    // 供基础设施层回填 ID（从数据库保存后获得）
    public void setId(OrderId id) {
        this.id = id;
    }

    // 只读访问，外部不能拿到可修改的列表
    public List<OrderItem> getItems() { return Collections.unmodifiableList(items); }

    public OrderId getId() { return id; }
    public UserId getUserId() { return userId; }
    public Money getTotalAmount() { return totalAmount; }
    public OrderStatus getStatus() { return status; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}
