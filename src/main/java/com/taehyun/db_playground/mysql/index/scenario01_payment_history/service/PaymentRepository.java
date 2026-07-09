package com.taehyun.db_playground.mysql.index.scenario01_payment_history.service;

import com.taehyun.db_playground.mysql.index.scenario01_payment_history.domain.Payment;
import com.taehyun.db_playground.mysql.index.scenario01_payment_history.domain.PaymentType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.List;

public interface PaymentRepository extends JpaRepository<Payment, Long> {

    @Query("SELECT p FROM Payment p " +
            "WHERE p.userId = :userId " +
            "AND p.paymentType = :paymentType " +
            "AND p.paymentStatus = 'SUCCESS' " +
            "AND p.createdAt BETWEEN :startCreatedAt AND :endCreatedAt " +
            "ORDER BY p.createdAt DESC")
    List<Payment> findSuccessPaymentsByPeriod(
            Long userId,
            PaymentType paymentType,
            LocalDateTime startCreatedAt,
            LocalDateTime endCreatedAt
    );

}
