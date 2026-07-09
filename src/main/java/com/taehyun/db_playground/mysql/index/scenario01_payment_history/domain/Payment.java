package com.taehyun.db_playground.mysql.index.scenario01_payment_history.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "payments_v1",
        indexes = {
                @Index(name = "idx_user_id", columnList = "user_id")
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "amount", nullable = false)
    private Long amount;

    @Enumerated(value = EnumType.STRING)
    @Column(name = "payment_type", nullable = false)
    private PaymentType paymentType;

    @Column(name = "payment_status", nullable = false)
    private String paymentStatus;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Builder
    public Payment(Long userId, Long amount, PaymentType paymentType, String paymentStatus, LocalDateTime createdAt) {
        this.userId = userId;
        this.amount = amount;
        this.paymentType = paymentType;
        this.paymentStatus = paymentStatus;
        this.createdAt = createdAt;
    }

}
