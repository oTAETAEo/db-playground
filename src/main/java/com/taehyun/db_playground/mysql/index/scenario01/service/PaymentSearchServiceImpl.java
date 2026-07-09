package com.taehyun.db_playground.mysql.index.scenario01.service;

import com.taehyun.db_playground.mysql.index.scenario01.domain.Payment;
import com.taehyun.db_playground.mysql.index.scenario01.domain.PaymentType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PaymentSearchServiceImpl implements PaymentSearchService{

    private final PaymentRepository paymentRepository;

    @Override
    public List<Payment> getSuccessPaymentsByPeriod(Long userId, PaymentType type, LocalDateTime searchStartAt, LocalDateTime searchEndAt) {

        return paymentRepository.findSuccessPaymentsByPeriod(userId, type, searchStartAt, searchEndAt);
    }

}
