---
layout: default
title: 04 - 一次完整请求的流程追踪
---

# 04 - 一次完整请求的流程追踪

## 场景：POST /orders 创建一个订单

### HTTP 请求体

```json
{
  "userId": 1,
  "items": [
    {
      "productId": 101,
      "productName": "Java 编程思想",
      "quantity": 2,
      "unitPrice": 89.00
    }
  ]
}
```

---

## 完整流程图

```
① HTTP 请求到达
        ↓
② OrderController.createOrder(request)          [interfaces 层]
   - 接收 CreateOrderRequest（JSON → Java 对象）
   - 转换为 CreateOrderCommand
        ↓
③ OrderApplicationService.createOrder(command)  [application 层]
   - @Transactional 开启事务
   - 构建 List<OrderItem>（用 Money、quantity 等）
   - 调用 Order.create(userId, items)
        ↓
④ Order.create(userId, items)                   [domain 层]
   - new Order(userId)，设 status = CREATED
   - 循环 addItem()，每次累加 totalAmount
   - validateTotalAmount()：178.00 > 0，通过
   - 返回 Order 聚合根对象
        ↓
⑤ orderRepository.save(order)                  [domain 层接口]
        ↓（依赖倒置，实际调用 infrastructure 实现）
⑥ OrderRepositoryImpl.save(order)              [infrastructure 层]
   - converter.toPO(order)
     → OrderPO { userId=1, totalAmount=178.00, status=CREATED }
     → OrderItemPO { productId=101, qty=2, unitPrice=89.00 }
   - jpaRepository.save(po)  →  INSERT INTO orders ...
                              →  INSERT INTO order_item ...
   - 数据库返回自增 ID = 1
   - converter.toDomain(savedPO)：重建 Order，回填 OrderId(1)
        ↓
⑦ 返回 savedOrder 给 ApplicationService
   - 取 savedOrder.getId().value() = 1L
        ↓
⑧ Controller 包装响应
   - new CreateOrderResponse(1L, "Order created successfully")
        ↓
⑨ HTTP 响应 200 OK
   { "orderId": 1, "message": "Order created successfully" }
```

---

## 各层传递的对象

| 步骤 | 传递对象 | 类型 |
|------|---------|------|
| ①→② | JSON body | HTTP |
| ②→③ | `CreateOrderCommand` | 应用层命令对象 |
| ③→④ | `UserId`, `List<OrderItem>` | 领域对象 |
| ④→⑤ | `Order` | 领域聚合根 |
| ⑤→⑥ | `Order` | 领域聚合根 |
| ⑥内部 | `OrderPO` | 持久化对象（基础设施内部） |
| ⑥→⑦ | `Order`（带 ID） | 领域聚合根 |
| ⑦→⑧ | `Long orderId` | 基本类型 |
| ⑧→⑨ | `CreateOrderResponse` | HTTP 响应 DTO |

---

## 关键观察点

1. **`OrderPO` 从未离开 `infrastructure` 包** — 领域层和接口层从未见过它
2. **`@Transactional` 只在 `application` 层** — 不在 Controller，不在 domain
3. **业务异常在 `domain` 层抛出** — `Order.create()` 金额为0时直接抛 `IllegalStateException`
4. **`OrderRepository` 接口对象在 `domain` 层定义** — `application` 层持有接口引用，不知道实现是 JPA

---

## 如何验证

启动项目后：

```bash
# 创建订单
curl -X POST http://localhost:8080/orders \
  -H "Content-Type: application/json" \
  -d '{
    "userId": 1,
    "items": [
      {
        "productId": 101,
        "productName": "Java 编程思想",
        "quantity": 2,
        "unitPrice": 89.00
      }
    ]
  }'

# 预期响应
{ "orderId": 1, "message": "Order created successfully" }

# 查看数据库（H2 控制台）
# 浏览器访问 http://localhost:8080/h2-console
# JDBC URL: jdbc:h2:mem:dddorder
# 用户名: sa，密码: 空
```
