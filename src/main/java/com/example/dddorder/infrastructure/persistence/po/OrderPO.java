package com.example.dddorder.infrastructure.persistence.po;

import com.example.dddorder.domain.order.OrderStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

// 订单持久化对象，映射到数据库 orders 表
// @Entity 只加在这里，领域层的 Order 是纯 Java 类，不受 JPA 污染
@Entity
@Table(name = "orders")
@Getter
@Setter
@NoArgsConstructor
public class OrderPO {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // 主键由数据库自增生成
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    // 总金额，数据库存 BigDecimal
    @Column(name = "total_amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal totalAmount;

    // 订单状态，以枚举名称字符串存储
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderStatus status;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    // 一对多关联明细，使用 CascadeType.ALL 让保存订单时自动保存明细
    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "order_id") // 明细表的外键列名
    private List<OrderItemPO> items = new ArrayList<>();
}
