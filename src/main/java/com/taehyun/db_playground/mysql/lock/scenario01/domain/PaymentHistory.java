package com.taehyun.db_playground.mysql.lock.scenario01.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(name = "payment_histories_v1")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PaymentHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long userId;

    private String itemId;

    private Long amount;

    @Enumerated(EnumType.STRING)
    private LockType lockType;

    private PaymentHistory(Long userId, Long amount, String itemId, LockType lockType) {
        this.userId = userId;
        this.amount = amount;
        this.itemId = itemId;
        this.lockType = lockType;
    }

    public static PaymentHistory createPaymentHistory(Long userId, Long amount, String itemId, LockType lockType) {
        return new PaymentHistory(userId, amount, itemId, lockType);
    }
}