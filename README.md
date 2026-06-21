# DDD 领域驱动设计学习项目

> 以"订单创建"为示例，用 JDK21 + Spring Boot 3 + Spring Data JPA 实现最小但完整的 DDD 四层架构。

## 技术栈

- JDK 21
- Spring Boot 3.2.5
- Spring Data JPA（基础设施层持久化）
- H2 内嵌数据库（无需安装，开箱即用）
- Lombok

## 快速启动

```bash
# 克隆项目
git clone https://github.com/hfutwz/DDD-Domain-FrameWork.git
cd DDD-Domain-FrameWork

# 构建并启动
./mvnw spring-boot:run

# 或者
mvn spring-boot:run
```

启动后访问：
- API：`http://localhost:8080/orders`
- H2 控制台：`http://localhost:8080/h2-console`（JDBC URL: `jdbc:h2:mem:dddorder`，用户名: `sa`，密码: 空）

---

## 包结构与文件说明

```
src/main/java/com/example/dddorder/
│
├── DddOrderApplication.java                      # Spring Boot 启动类
│
├── interfaces/                                    # ① 用户接口层：只做协议转换
│   └── rest/
│       ├── OrderController.java                   # REST 控制器，接收 HTTP 请求，调用应用服务
│       └── dto/
│           ├── CreateOrderRequest.java            # 入参 DTO，承载 JSON 请求数据
│           └── CreateOrderResponse.java           # 出参 DTO，封装响应数据
│
├── application/                                   # ② 应用服务层：编排用例，开启事务
│   ├── OrderApplicationService.java              # 创建订单用例的步骤编排，含 @Transactional
│   └── command/
│       └── CreateOrderCommand.java               # 命令对象，从 DTO 转换而来，传入应用服务
│
├── domain/                                        # ③ 领域层：核心业务，纯 Java，零框架依赖
│   └── order/
│       ├── Order.java                            # 聚合根：订单的一致性边界，封装业务规则
│       ├── OrderItem.java                        # 实体：订单明细，属于 Order 聚合内部
│       ├── OrderStatus.java                      # 枚举：订单状态（CREATED/PAID/CANCELLED）
│       ├── repository/
│       │   └── OrderRepository.java             # 仓储接口：领域层定义，描述"需要什么持久化能力"
│       └── valueobject/
│           ├── OrderId.java                      # 值对象：订单ID，不可变，构造时校验
│           ├── UserId.java                       # 值对象：用户ID，不可变，构造时校验
│           └── Money.java                        # 值对象：金额，封装货币计算，防止负数
│
└── infrastructure/                                # ④ 基础设施层：实现领域接口，负责持久化
    └── persistence/
        ├── OrderRepositoryImpl.java              # 仓储实现：实现 OrderRepository 接口，协调 JPA 和转换器
        ├── po/
        │   ├── OrderPO.java                     # 订单持久化对象：@Entity 映射 orders 表
        │   └── OrderItemPO.java                 # 明细持久化对象：@Entity 映射 order_item 表
        ├── repository/
        │   └── OrderJpaRepository.java          # Spring Data JPA 接口，基础设施内部使用
        └── converter/
            └── OrderConverter.java              # 转换器：Order ↔ OrderPO 双向转换
```

---

## DDD 四层职责速查

| 层 | 职责 | 能写什么 | 不能写什么 |
|----|------|---------|-----------|
| **interfaces** | 协议转换 | Controller、DTO、JSON 映射 | 业务逻辑、数据库操作 |
| **application** | 用例编排 | 开事务、调用领域层、调用仓储 | 业务规则（if 条件判断） |
| **domain** | 业务核心 | 聚合根、实体、值对象、仓储接口 | Spring 注解、JPA 注解、数据库字段名 |
| **infrastructure** | 持久化实现 | PO、JPA、Converter、仓储实现 | 业务规则 |

---

## 业务规则说明

| 规则 | 位置 | 实现方式 |
|------|------|---------|
| 订单初始状态为 CREATED | `Order` 构造方法 | `this.status = OrderStatus.CREATED` |
| 总金额由 OrderItem 汇总计算 | `Order.addItem()` | 累加每个明细的 `subtotal()` |
| 总金额必须大于 0 | `Order.validateTotalAmount()` | 抛 `IllegalStateException` |
| OrderId/UserId 不能为负 | 值对象构造方法 | 抛 `IllegalArgumentException` |
| Money 不能为负数 | `Money` 构造方法 | 抛 `IllegalArgumentException` |

---

## 依赖倒置示意

```
domain 层定义接口：
  OrderRepository（接口）

infrastructure 层实现接口：
  OrderRepositoryImpl implements OrderRepository

application 层依赖接口，不依赖实现：
  private final OrderRepository orderRepository; // 运行时注入 Impl
```

领域层不知道 JPA 的存在，换成 MyBatis 只需替换 `OrderRepositoryImpl`，领域层零修改。

---

## 学习文档

| 文件 | 内容 |
|------|------|
| [docs/01-ddd-layers.md](docs/01-ddd-layers.md) | 四层架构说明与包结构总览 |
| [docs/02-domain-model.md](docs/02-domain-model.md) | 聚合根、实体、值对象详解 |
| [docs/03-dependency-inversion.md](docs/03-dependency-inversion.md) | 依赖倒置原则与 PO 隔离说明 |
| [docs/04-flow-walkthrough.md](docs/04-flow-walkthrough.md) | 一次完整 HTTP 请求的流程追踪 |

---

## 测试接口

```bash
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
```

预期响应：
```json
{
  "orderId": 1,
  "message": "Order created successfully"
}
```
