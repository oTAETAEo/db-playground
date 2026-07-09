package com.taehyun.db_playground.mysql.lock.scenario01.service;

public interface WalletService {

    void decreaseBalance(Long userId, Long amount);

}
