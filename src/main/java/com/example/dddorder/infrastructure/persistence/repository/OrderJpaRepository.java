package com.example.dddorder.infrastructure.persistence.repository;

import com.example.dddorder.infrastructure.persistence.po.OrderPO;
import org.springframework.data.jpa.repository.JpaRepository;

// Spring Data JPA 仓储，只操作 PO 对象，是基础设施层内部的细节
// 领域层不知道这个接口的存在
public interface OrderJpaRepository extends JpaRepository<OrderPO, Long> {
    // 单表 CRUD 由 JpaRepository 默认提供，无需手写
}
