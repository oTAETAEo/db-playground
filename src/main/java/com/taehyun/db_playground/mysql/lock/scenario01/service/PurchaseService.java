package com.taehyun.db_playground.mysql.lock.scenario01.service;

import com.taehyun.db_playground.mysql.lock.scenario01.domain.LockType;
import com.taehyun.db_playground.mysql.lock.scenario01.domain.PaymentHistory;
import com.taehyun.db_playground.mysql.lock.scenario01.repository.PaymentHistoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PurchaseService {

    private final WalletService walletService;

    private final PaymentHistoryRepository paymentHistoryRepository;

    @Transactional
    public void purchaseNoLock(Long userId, Long amount, String itemId) {

        walletService.decreaseBalance(userId, amount);

        paymentHistoryRepository.save(PaymentHistory.createPaymentHistory(userId, amount, itemId, LockType.NO_LOCK));
    }

    @Transactional
    public void purchaseOptimistic(Long userId, Long amount, String itemId) {

        walletService.decreaseBalanceOptimistic(userId, amount);

        paymentHistoryRepository.save(PaymentHistory.createPaymentHistory(userId, amount, itemId, LockType.OPTIMISTIC));
    }

    @Transactional
    public void purchasePessimistic(Long userId, Long amount, String itemId){

        walletService.decreaseBalancePessimistic(userId, amount);

        paymentHistoryRepository.save(PaymentHistory.createPaymentHistory(userId, amount, itemId, LockType.PESSIMISTIC));
    }

}
