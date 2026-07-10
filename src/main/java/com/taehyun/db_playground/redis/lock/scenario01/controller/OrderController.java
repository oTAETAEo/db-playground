package com.taehyun.db_playground.redis.lock.scenario01.controller;

import com.taehyun.db_playground.redis.lock.scenario01.service.OrderItem;
import com.taehyun.db_playground.redis.lock.scenario01.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController("idempotencyOrderController")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @PostMapping("/order")
    public ResponseEntity<OrderResponse> order(
            @RequestHeader("Idempotency-Key") String idempotencyKey,
            @RequestBody OrderRequest orderRequest
    ) {

        Long userId = orderRequest.userId;
        List<OrderItem> items = orderRequest.items;

        orderService.createOrder(userId, idempotencyKey, items);

        return ResponseEntity.ok(new OrderResponse("주문 성공"));
    }

    public record OrderRequest(
            Long userId,
            List<OrderItem> items
    ){}

    public record OrderResponse(
            String message
    ) {}
}

