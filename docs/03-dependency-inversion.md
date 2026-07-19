---
layout: default
title: 03 - 依赖倒置详解
---

# 03 - 依赖倒置详解

## 问题：领域层不能依赖数据库

DDD 的核心要求之一：**领域层是纯业务逻辑，不能知道数据库的存在**。

但领域层又需要保存数据，怎么办？

---

## 解法：依赖倒置原则（DIP）

```
传统做法（错误）：
  领域层 Order → 直接依赖 → JPA Repository（基础设施）
  问题：换数据库时领域层也要改

DDD 做法（正确）：
  领域层 Order → 依赖 → OrderRepository（接口，定义在领域层）
                              ↑ 实现
                     OrderRepositoryImpl（基础设施层）→ JPA
```

**"倒置"在哪里？**

- 传统：高层（业务）依赖低层（数据库技术）
- DIP：高层（业务）只依赖接口，低层实现接口，**依赖关系倒过来了**

---

## 本项目中的实现

### 第一步：领域层定义接口

```java
// domain/order/repository/OrderRepository.java
// 这个接口在领域层，只用 Java 原生类型，没有任何框架依赖
public interface OrderRepository {
    Order save(Order order);
    Optional<Order> findById(OrderId orderId);
}
```

### 第二步：基础设施层实现接口

```java
// infrastructure/persistence/OrderRepositoryImpl.java
@Repository  // Spring 注解只在基础设施层出现
public class OrderRepositoryImpl implements OrderRepository {
    // 内部用 JPA，外部（领域层）不知道
    private final OrderJpaRepository jpaRepository;
    ...
}
```

### 第三步：应用层注入接口，不注入实现

```java
// application/OrderApplicationService.java
public class OrderApplicationService {
    // 依赖接口类型，运行时 Spring 注入 OrderRepositoryImpl
    private final OrderRepository orderRepository;
}
```

---

## PO 隔离是关键

```
❌ 错误做法：直接在 Order 上加 @Entity
   问题：领域层被 JPA 侵入，换 ORM 框架时领域层要改

✅ 正确做法：建 OrderPO 专门用于数据库映射

领域对象（纯 Java）          持久化对象（JPA）
Order                ←→    OrderPO（@Entity）
  OrderItem          ←→    OrderItemPO（@Entity）
  Money              ←→    BigDecimal（数据库字段）

        OrderConverter 负责双向转换
```

---

## 依赖方向总结

```
interfaces ──→ application ──→ domain ←── infrastructure
（只调用）    （编排+事务）   （核心）    （实现domain接口）

箭头方向 = 依赖方向
domain 是最内层，没有任何向外的箭头
```
