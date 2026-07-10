package com.taehyun.db_playground.redis.lock.scenario01.service;

import java.util.List;

public interface OrderService {

    void createOrder(Long userId, String idempotencyKey, List<OrderItem> items);
}
