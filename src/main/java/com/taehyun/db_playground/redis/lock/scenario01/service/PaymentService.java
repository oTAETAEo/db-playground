package com.taehyun.db_playground.redis.lock.scenario01.service;

public interface PaymentService {

    void process(Long userId, long amount, String idempotencyKey);

}
