package com.taehyun.db_playground.mysql.index.scenario01_payment_history.service;

import com.taehyun.db_playground.mysql.index.scenario01_payment_history.domain.Payment;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PaymentSearchServiceImpl implements PaymentSearchService{

    @Override
    public List<Payment> getRecentCardPayments(Long userId) {
        return List.of();
    }

}
