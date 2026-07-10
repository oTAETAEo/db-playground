package com.taehyun.db_playground.redis.lock.scenario01.service;

import com.taehyun.db_playground.redis.lock.scenario01.domain.PaymentTransaction;
import com.taehyun.db_playground.redis.lock.scenario01.domain.Wallet;
import com.taehyun.db_playground.redis.lock.scenario01.repositroy.PaymentTransactionRepository;
import com.taehyun.db_playground.redis.lock.scenario01.repositroy.WalletRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service("idempotencyPaymentServiceImpl")
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {

    private final PaymentTransactionRepository paymentTransactionRepository;

    private final WalletRepository walletRepository;

    @Override
    @Transactional
    public void process(Long userId, long amount, String idempotencyKey) {

        Wallet wallet = walletRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("해당 유저(ID: " + userId + ")의 지갑을 찾을 수 없습니다."));

        wallet.deduct(amount);

        paymentTransactionRepository.save(
                PaymentTransaction.createPaymentTransaction(userId, amount, idempotencyKey));

    }

}
