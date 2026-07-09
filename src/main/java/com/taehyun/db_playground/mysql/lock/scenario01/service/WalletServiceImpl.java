package com.taehyun.db_playground.mysql.lock.scenario01.service;

import com.taehyun.db_playground.mysql.lock.scenario01.domain.WalletNoLock;
import com.taehyun.db_playground.mysql.lock.scenario01.domain.WalletOptimistic;
import com.taehyun.db_playground.mysql.lock.scenario01.domain.WalletPessimistic;
import com.taehyun.db_playground.mysql.lock.scenario01.repository.WalletOptimisticRepository;
import com.taehyun.db_playground.mysql.lock.scenario01.repository.WalletNoLockRepository;
import com.taehyun.db_playground.mysql.lock.scenario01.repository.WalletPessimisticRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
public class WalletServiceImpl implements WalletService {

    private final WalletNoLockRepository walletNoLockRepository;
    private final WalletOptimisticRepository walletOptimisticRepository;
    private final WalletPessimisticRepository walletPessimisticRepository;

    @Override
    @Transactional
    public void decreaseBalance(Long userId, Long amount) {

        WalletNoLock walletNoLock = walletNoLockRepository.findByUserId(userId)
                .orElseThrow(NoSuchElementException::new);

        walletNoLock.deduct(amount);
    }

    @Override
    @Transactional
    public void decreaseBalanceOptimistic(Long userId, Long amount) {

        WalletOptimistic walletOptimistic = walletOptimisticRepository.findByUserId(userId)
                .orElseThrow(NoSuchElementException::new);

        walletOptimistic.deduct(amount);
    }

    @Override
    @Transactional
    public void decreaseBalancePessimistic(Long userId, Long amount) {

        WalletPessimistic walletPessimistic = walletPessimisticRepository.findByUserId(userId)
                .orElseThrow(NoSuchElementException::new);

        walletPessimistic.deduct(amount);
    }

}
