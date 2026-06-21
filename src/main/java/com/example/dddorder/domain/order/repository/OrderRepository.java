package com.example.dddorder.domain.order.repository;

import com.example.dddorder.domain.order.Order;
import com.example.dddorder.domain.order.valueobject.OrderId;

import java.util.Optional;

// 仓储接口，定义在领域层，描述"需要什么持久化能力"，不关心怎么实现
// 实现由基础设施层提供，通过依赖倒置注入（领域层依赖接口，不依赖实现）
public interface OrderRepository {

    // 保存订单，返回带有数据库生成 ID 的订单
    Order save(Order order);

    // 根据订单ID查询订单
    Optional<Order> findById(OrderId orderId);
}
