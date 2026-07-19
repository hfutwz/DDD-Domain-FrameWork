---
layout: default
title: 01 - DDD 四层架构说明
---

# 01 - DDD 四层架构说明

## 核心思想

DDD 的四层架构让每一层只做自己该做的事，层与层之间依赖关系单向向内：

```
interfaces（最外层）
    ↓ 调用
application（应用层）
    ↓ 调用
domain（最内层，核心）
    ↑ 实现（依赖倒置）
infrastructure（基础设施层）
```

> **关键规则：依赖只能从外向内，内层不知道外层的存在。**

---

## 各层职责对比

| 层 | 包名 | 职责 | 不能做什么 |
|----|------|------|-----------|
| 用户接口层 | `interfaces` | 接收 HTTP 请求，转换格式，返回响应 | 不写业务逻辑，不直接操作数据库 |
| 应用服务层 | `application` | 编排用例步骤，开启事务 | 不写业务规则（if 金额 > 0 这种逻辑不在这里） |
| 领域层 | `domain` | 定义业务规则、聚合根、值对象、仓储接口 | 不引入 Spring/JPA 注解，不知道数据库存在 |
| 基础设施层 | `infrastructure` | 实现仓储接口，映射数据库，处理 IO | 不写业务规则 |

---

## 与传统分层的对比

你熟悉的传统写法：

```
Controller → Service → Mapper/DAO → 数据库
```

DDD 写法：

```
Controller（interfaces）
    → ApplicationService（application）
        → Order 聚合根执行业务规则（domain）
            → OrderRepository 接口（domain 定义）
                ← OrderRepositoryImpl 实现（infrastructure）
                    → JPA/MyBatis → 数据库
```

最大的区别：**业务规则从 Service 移进了领域对象本身**，Service 变成了纯粹的"步骤编排者"。

---

## 本项目包结构

```
com.example.dddorder/
├── interfaces/
│   └── rest/
│       ├── OrderController.java          # HTTP 入口
│       └── dto/
│           ├── CreateOrderRequest.java   # 入参
│           └── CreateOrderResponse.java  # 出参
│
├── application/
│   ├── OrderApplicationService.java      # 用例编排
│   └── command/
│       └── CreateOrderCommand.java       # 命令对象
│
├── domain/
│   └── order/
│       ├── Order.java                    # 聚合根
│       ├── OrderItem.java               # 实体
│       ├── OrderStatus.java             # 枚举
│       ├── repository/
│       │   └── OrderRepository.java     # 仓储接口（领域层定义）
│       └── valueobject/
│           ├── OrderId.java             # 值对象
│           ├── UserId.java              # 值对象
│           └── Money.java               # 值对象
│
└── infrastructure/
    └── persistence/
        ├── OrderRepositoryImpl.java      # 仓储实现
        ├── po/
        │   ├── OrderPO.java             # 订单持久化对象
        │   └── OrderItemPO.java         # 明细持久化对象
        ├── repository/
        │   └── OrderJpaRepository.java  # Spring Data JPA 接口
        └── converter/
            └── OrderConverter.java      # 领域对象 ↔ PO 转换
```
