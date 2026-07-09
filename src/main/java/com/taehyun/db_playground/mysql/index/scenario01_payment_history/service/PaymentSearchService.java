package com.taehyun.db_playground.mysql.index.scenario01_payment_history.service;

import com.taehyun.db_playground.mysql.index.scenario01_payment_history.domain.Payment;
import com.taehyun.db_playground.mysql.index.scenario01_payment_history.domain.PaymentType;

import java.time.LocalDateTime;
import java.util.List;

public interface PaymentSearchService {

    List<Payment> getSuccessPaymentsByPeriod(Long userId, PaymentType type, LocalDateTime searchStartAt, LocalDateTime searchEndAt);

}
