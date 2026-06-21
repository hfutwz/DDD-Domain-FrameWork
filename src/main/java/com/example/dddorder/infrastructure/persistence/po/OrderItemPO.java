package com.example.dddorder.infrastructure.persistence.po;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

// 订单明细持久化对象，映射到数据库 order_item 表
// 注意：@Entity 只出现在这里，领域层的 OrderItem 没有任何 JPA 注解
@Entity
@Table(name = "order_item")
@Getter
@Setter
@NoArgsConstructor
public class OrderItemPO {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 关联的订单ID（外键），使用冗余字段而非 @ManyToOne 引用，简化聚合加载
    @Column(name = "order_id", nullable = false)
    private Long orderId;

    @Column(name = "product_id", nullable = false)
    private Long productId;

    @Column(name = "product_name", nullable = false)
    private String productName;

    @Column(nullable = false)
    private Integer quantity;

    // 单价，数据库存 BigDecimal，转换器负责与 Money 值对象互转
    @Column(name = "unit_price", nullable = false, precision = 10, scale = 2)
    private BigDecimal unitPrice;
}
