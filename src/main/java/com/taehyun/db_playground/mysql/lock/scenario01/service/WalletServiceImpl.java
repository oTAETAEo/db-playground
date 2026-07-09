package com.taehyun.db_playground.mysql.lock.scenario01.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class WalletServiceImpl implements WalletService {

    @Override
    public void decreaseBalance(Long userId, Long amount) {

    }

}
