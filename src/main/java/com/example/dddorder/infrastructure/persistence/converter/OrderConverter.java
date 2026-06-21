package com.example.dddorder.infrastructure.persistence.converter;

import com.example.dddorder.domain.order.Order;
import com.example.dddorder.domain.order.OrderItem;
import com.example.dddorder.domain.order.valueobject.Money;
import com.example.dddorder.domain.order.valueobject.OrderId;
import com.example.dddorder.domain.order.valueobject.UserId;
import com.example.dddorder.infrastructure.persistence.po.OrderItemPO;
import com.example.dddorder.infrastructure.persistence.po.OrderPO;
import org.springframework.stereotype.Component;

import java.util.List;

// 转换器：负责领域对象 ↔ 持久化对象 的双向转换
// 这是 DDD 中实现"防腐层"的关键，领域层与数据库字段结构完全解耦
@Component
public class OrderConverter {

    // 领域对象 → PO（准备存入数据库）
    public OrderPO toPO(Order order) {
        OrderPO po = new OrderPO();
        // 新订单 ID 可能为 null，保存后由数据库生成
        if (order.getId() != null) {
            po.setId(order.getId().value());
        }
        po.setUserId(order.getUserId().value());
        po.setTotalAmount(order.getTotalAmount().amount());
        po.setStatus(order.getStatus());
        po.setCreatedAt(order.getCreatedAt());

        // 转换订单明细列表
        List<OrderItemPO> itemPOs = order.getItems().stream()
                .map(this::toItemPO)
                .toList();
        po.setItems(itemPOs);
        return po;
    }

    // 明细领域对象 → 明细 PO
    private OrderItemPO toItemPO(OrderItem item) {
        OrderItemPO po = new OrderItemPO();
        po.setProductId(item.getProductId());
        po.setProductName(item.getProductName());
        po.setQuantity(item.getQuantity());
        po.setUnitPrice(item.getUnitPrice().amount());
        return po;
    }

    // PO → 领域对象（从数据库读取后还原为领域模型）
    public Order toDomain(OrderPO po) {
        // 通过工厂方法重建明细列表
        List<OrderItem> items = po.getItems().stream()
                .map(itemPO -> new OrderItem(
                        itemPO.getProductId(),
                        itemPO.getProductName(),
                        itemPO.getQuantity(),
                        Money.of(itemPO.getUnitPrice())
                ))
                .toList();

        // 重建订单聚合根（工厂方法会重新计算总金额）
        Order order = Order.create(new UserId(po.getUserId()), items);
        // 回填数据库生成的 ID
        order.setId(new OrderId(po.getId()));
        return order;
    }
}
