package com.example.dddorder.interfaces.rest;

import com.example.dddorder.application.OrderApplicationService;
import com.example.dddorder.application.command.CreateOrderCommand;
import com.example.dddorder.interfaces.rest.dto.CreateOrderRequest;
import com.example.dddorder.interfaces.rest.dto.CreateOrderResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

// 用户接口层：只做协议转换（HTTP JSON → Command → Response），不写业务逻辑
@RestController
@RequestMapping("/orders")
public class OrderController {

    // 依赖应用服务，由 Spring 注入
    private final OrderApplicationService orderApplicationService;

    public OrderController(OrderApplicationService orderApplicationService) {
        this.orderApplicationService = orderApplicationService;
    }

    // POST /orders - 创建订单接口
    @PostMapping
    public ResponseEntity<CreateOrderResponse> createOrder(@RequestBody CreateOrderRequest request) {
        // 将 HTTP 请求 DTO 转换为应用层命令对象
        CreateOrderCommand command = new CreateOrderCommand(
                request.userId(),
                request.items().stream()
                        .map(item -> new CreateOrderCommand.Item(
                                item.productId(),
                                item.productName(),
                                item.quantity(),
                                item.unitPrice()
                        ))
                        .toList()
        );

        // 调用应用服务执行用例，获得订单ID
        Long orderId = orderApplicationService.createOrder(command);

        // 将结果封装为响应 DTO 返回
        return ResponseEntity.ok(new CreateOrderResponse(orderId, "Order created successfully"));
    }
}
