package com.example.dddorder.application;

import com.example.dddorder.application.command.CreateOrderCommand;
import com.example.dddorder.domain.order.Order;
import com.example.dddorder.domain.order.OrderItem;
import com.example.dddorder.domain.order.repository.OrderRepository;
import com.example.dddorder.domain.order.valueobject.Money;
import com.example.dddorder.domain.order.valueobject.UserId;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

// 应用服务：编排"创建订单"用例的执行步骤，不包含任何业务规则
// 只负责：开事务、构建领域对象、调用领域方法、调用仓储保存
@Service
public class OrderApplicationService {

    // 依赖领域层的仓储接口，不依赖具体实现
    private final OrderRepository orderRepository;

    public OrderApplicationService(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    // 创建订单用例入口，事务在应用服务层开启
    @Transactional
    public Long createOrder(CreateOrderCommand command) {
        // 1. 将命令中的明细转换为领域对象 OrderItem
        List<OrderItem> items = command.items().stream()
                .map(item -> new OrderItem(
                        item.productId(),
                        item.productName(),
                        item.quantity(),
                        Money.of(item.unitPrice())
                ))
                .toList();

        // 2. 调用聚合根工厂方法创建订单（业务规则在此执行）
        Order order = Order.create(new UserId(command.userId()), items);

        // 3. 通过仓储接口保存订单（不知道也不关心底层怎么存）
        Order savedOrder = orderRepository.save(order);

        // 4. 返回生成的订单ID
        return savedOrder.getId().value();
    }
}
