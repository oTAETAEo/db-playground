package com.taehyun.db_playground.mysql.index.scenario01.service;

import com.taehyun.db_playground.mysql.index.scenario01.domain.Payment;
import com.taehyun.db_playground.mysql.index.scenario01.domain.PaymentType;

import java.time.LocalDateTime;
import java.util.List;

public interface PaymentSearchService {

    List<Payment> getSuccessPaymentsByPeriod(Long userId, PaymentType type, LocalDateTime searchStartAt, LocalDateTime searchEndAt);

}
