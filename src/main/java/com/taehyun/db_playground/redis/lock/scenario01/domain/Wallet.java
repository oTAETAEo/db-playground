package com.taehyun.db_playground.redis.lock.scenario01.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "wallet_v1")
public class Wallet {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private Long userId;

    @Column(nullable = false)
    private long balance;

    private Wallet(Long userId, long balance) {
        this.userId = userId;
        this.balance = balance;
    }

    public static Wallet createWallet(Long userId, long balance){
        return new Wallet(userId, balance);
    }

    public long increaseBalance(long money){

        if(money <= 0)
            throw new IllegalArgumentException("잔액 증가 돈이 0이거나 음수일 수 없습니다");

        balance += money;

        return balance;
    }

    public void balanceDeduct(long amount) {

        if (amount == 0)
            throw new IllegalArgumentException("차감할 금액이 0 일 수 없습니다");

        if (this.balance < amount)
            throw new IllegalArgumentException("잔고가 부족합니다");

        this.balance -= amount;
    }

}
