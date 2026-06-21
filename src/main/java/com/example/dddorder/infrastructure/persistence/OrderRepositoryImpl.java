package com.example.dddorder.infrastructure.persistence;

import com.example.dddorder.domain.order.Order;
import com.example.dddorder.domain.order.repository.OrderRepository;
import com.example.dddorder.domain.order.valueobject.OrderId;
import com.example.dddorder.infrastructure.persistence.converter.OrderConverter;
import com.example.dddorder.infrastructure.persistence.po.OrderPO;
import com.example.dddorder.infrastructure.persistence.repository.OrderJpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

// 仓储实现：实现领域层定义的 OrderRepository 接口
// 这是依赖倒置原则的体现：领域层定义接口，基础设施层提供实现
// 领域层依赖接口（稳定），基础设施层依赖具体技术（可替换）
@Repository
public class OrderRepositoryImpl implements OrderRepository {

    private final OrderJpaRepository jpaRepository; // Spring Data JPA，负责实际 SQL 操作
    private final OrderConverter converter;         // 转换器，负责对象互转

    public OrderRepositoryImpl(OrderJpaRepository jpaRepository, OrderConverter converter) {
        this.jpaRepository = jpaRepository;
        this.converter = converter;
    }

    @Override
    public Order save(Order order) {
        // 1. 领域对象 → PO
        OrderPO po = converter.toPO(order);
        // 2. JPA 保存，返回带自增 ID 的 PO
        OrderPO savedPO = jpaRepository.save(po);
        // 3. PO → 领域对象，携带数据库生成的 ID 返回给应用层
        return converter.toDomain(savedPO);
    }

    @Override
    public Optional<Order> findById(OrderId orderId) {
        // 查询 PO，转换为领域对象返回
        return jpaRepository.findById(orderId.value())
                .map(converter::toDomain);
    }
}
