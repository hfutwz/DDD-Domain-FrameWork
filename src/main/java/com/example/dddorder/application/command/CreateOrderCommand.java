package com.example.dddorder.application.command;

import java.math.BigDecimal;
import java.util.List;

// 命令对象：封装"创建订单"这个用例所需的输入数据
// 从 interfaces 层的 DTO 转换而来，传入应用服务层
public record CreateOrderCommand(
        Long userId,       // 下单用户ID
        List<Item> items   // 订单明细列表
) {
    // 订单明细命令对象，嵌套在命令内部
    public record Item(
            Long productId,       // 商品ID
            String productName,   // 商品名称
            int quantity,         // 购买数量
            BigDecimal unitPrice  // 单价
    ) {}
}
