package com.taehyun.db_playground.mysql.index.scenario01_payment_history.service;

import com.taehyun.db_playground.mysql.index.scenario01_payment_history.domain.Payment;

import java.util.List;

public interface PaymentSearchService {

    List<Payment> getRecentCardPayments(Long userId);

}
