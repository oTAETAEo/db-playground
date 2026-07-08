package com.taehyun.db_playground.mysql.index.scenario01_payment_history.service;

import com.taehyun.db_playground.mysql.index.scenario01_payment_history.domain.Payment;
import com.taehyun.db_playground.mysql.index.scenario01_payment_history.domain.PaymentType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PaymentSearchServiceImpl implements PaymentSearchService{

    private final PaymentRepository paymentRepository;

    @Override
    public List<Payment> getPaymentsByPeriod(Long userId, PaymentType type, LocalDateTime searchStartAt, LocalDateTime searchEndAt) {

        return paymentRepository.findPaymentsByPeriod(userId, type, searchStartAt, searchEndAt);
    }

}
