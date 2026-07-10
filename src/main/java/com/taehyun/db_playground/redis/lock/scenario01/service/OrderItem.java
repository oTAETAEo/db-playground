package com.taehyun.db_playground.redis.lock.scenario01.service;

public record OrderItem(
        String name,
        long price,
        long count
){}