package com.taehyun.db_playground.mysql.lock.scenario01.service;

import com.taehyun.db_playground.mysql.lock.scenario01.domain.Wallet;
import com.taehyun.db_playground.mysql.lock.scenario01.repository.WalletRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
public class WalletServiceImpl implements WalletService {

    private final WalletRepository walletRepository;

    @Override
    @Transactional
    public void decreaseBalance(Long userId, Long amount) {

        Wallet wallet = walletRepository.findByUserId(userId)
                .orElseThrow(NoSuchElementException::new);

        wallet.deduct(amount);
    }

}
