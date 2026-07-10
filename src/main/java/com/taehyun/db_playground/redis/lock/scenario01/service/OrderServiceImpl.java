package com.taehyun.db_playground.redis.lock.scenario01.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service("idempotencyOrderService")
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final PaymentService paymentService;

    @Override
    @Transactional
    public void createOrder(Long userId, String idempotencyKey, List<OrderItem> items) {

        // (주문 생성)  내부 Order 컬럼 저장 ...


        // 결제 시작
        long totalPrice = totalPriceCalculator(items);

        paymentService.process(userId, totalPrice, idempotencyKey);

    }


    private long totalPriceCalculator(List<OrderItem> items){

        return items.stream()
                .mapToLong(i -> i.price() * i.count())
                .sum();
    }


}
