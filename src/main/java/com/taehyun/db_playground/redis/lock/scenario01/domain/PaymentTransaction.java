package com.taehyun.db_playground.redis.lock.scenario01.domain;


import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(
        name = "redis_scenario_payment_transaction",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_redis_user_idempotency",
                        columnNames = {"userId", "idempotencyKey"}
                )
        }
)
public class PaymentTransaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long userId;

    @Column(nullable = false)
    private int amount;

    @Column(nullable = false)
    private String idempotencyKey;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    private PaymentTransaction(Long userId, int amount, String idempotencyKey) {
        this.userId = userId;
        this.amount = amount;
        this.idempotencyKey = idempotencyKey;
        this.createdAt = LocalDateTime.now();
    }

    public static PaymentTransaction createPaymentTransaction(Long userId, int amount, String idempotencyKey){
        return new PaymentTransaction(userId, amount, idempotencyKey);
    }

}
