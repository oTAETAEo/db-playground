package com.taehyun.db_playground.redis.lock.scenario01.service;

public interface PaymentService {

    Long process(Long userId, long amount, String idempotencyKey);

}
