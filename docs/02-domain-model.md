# 02 - 领域模型详解

## 核心概念速查

| 概念 | 本项目对应 | 特征 |
|------|-----------|------|
| 聚合根（Aggregate Root） | `Order` | 整个订单的唯一入口，控制内部一致性 |
| 实体（Entity） | `OrderItem` | 有生命周期，属于 Order 聚合，不独立存在 |
| 值对象（Value Object） | `Money` `OrderId` `UserId` | 不可变，没有 ID，靠值判断相等 |
| 仓储接口（Repository） | `OrderRepository` | 领域层定义，描述持久化需求，不关心实现 |

---

## 聚合根：Order

```java
// 聚合根是一致性边界的守护者
// 所有对订单的修改，必须通过 Order 的方法进行
public class Order {
    private OrderId id;
    private UserId userId;
    private List<OrderItem> items;  // 内部实体，外部不能直接操作
    private Money totalAmount;       // 由 items 计算，不能外部设置
    private OrderStatus status;      // 初始固定为 CREATED
}
```

**关键设计：**
- `items` 是私有的，返回时用 `Collections.unmodifiableList()` 包装，外部无法添加/删除
- `totalAmount` 由 `addItem()` 内部计算，外部无法直接赋值
- 工厂方法 `Order.create()` 保证业务规则（金额 > 0）在创建时就被执行

---

## 值对象：Money

```java
// record 关键字保证：不可变、equals/hashCode 自动按值实现
public record Money(BigDecimal amount) {
    // 相加返回新对象，原对象不变
    public Money add(Money other) {
        return new Money(this.amount.add(other.amount));
    }
}
```

**为什么不用 BigDecimal 直接传？**

| 直接用 BigDecimal | 用 Money 值对象 |
|------------------|----------------|
| `order.setPrice(BigDecimal.valueOf(100))` | `order.setPrice(Money.of(BigDecimal.valueOf(100)))` |
| 可以传入负数，无保护 | 构造时校验，不合法直接抛异常 |
| 含义模糊（是价格？总额？折扣？） | 语义明确，就是"金额" |

---

## 业务规则在哪里执行？

```
Order.create(userId, items)
    ├── addItem(item)        ← 计算每个明细的小计，累加总金额
    └── validateTotalAmount() ← 业务规则：总金额必须 > 0，否则抛异常
```

**传统写法**会把这个校验放在 Service：
```java
// ❌ 传统做法，业务规则散落在 Service
if (order.getTotalAmount().compareTo(BigDecimal.ZERO) <= 0) {
    throw new Exception("金额必须大于0");
}
```

**DDD 做法**，规则封装在聚合根内部，Service 不用管：
```java
// ✅ DDD 做法，调用工厂方法时自动执行
Order order = Order.create(userId, items); // 金额 <= 0 时这里直接抛异常
```

---

## 聚合内部结构图

```
Order（聚合根）
  ├── OrderId（值对象）        → 标识这个 Order
  ├── UserId（值对象）         → 标识下单用户
  ├── Money totalAmount（值对象）→ 总金额，由 items 计算
  ├── OrderStatus（枚举）      → 订单状态
  └── List<OrderItem>（实体）
        ├── productId
        ├── productName
        ├── quantity
        └── Money unitPrice（值对象）
```
