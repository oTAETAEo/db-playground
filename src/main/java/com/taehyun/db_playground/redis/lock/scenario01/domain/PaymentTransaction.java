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
        name = "payment_transaction_v1",
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
    private long totalAmount;

    @Column(nullable = false)
    private String idempotencyKey;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    private PaymentTransaction(Long userId, long totalAmount, String idempotencyKey) {
        this.userId = userId;
        this.totalAmount = totalAmount;
        this.idempotencyKey = idempotencyKey;
        this.createdAt = LocalDateTime.now();
    }

    public static PaymentTransaction createPaymentTransaction(Long userId, long totalAmount, String idempotencyKey){
        return new PaymentTransaction(userId, totalAmount, idempotencyKey);
    }

}
