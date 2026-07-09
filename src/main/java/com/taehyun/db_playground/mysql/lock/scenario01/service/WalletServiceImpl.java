package com.taehyun.db_playground.mysql.lock.scenario01.service;

import com.taehyun.db_playground.mysql.lock.scenario01.domain.WalletNoLock;
import com.taehyun.db_playground.mysql.lock.scenario01.repository.WalletNoLockRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
public class WalletServiceImpl implements WalletService {

    private final WalletNoLockRepository walletNoLockRepository;

    @Override
    @Transactional
    public void decreaseBalance(Long userId, Long amount) {

        WalletNoLock walletNoLock = walletNoLockRepository.findByUserId(userId)
                .orElseThrow(NoSuchElementException::new);

        walletNoLock.deduct(amount);
    }

}
