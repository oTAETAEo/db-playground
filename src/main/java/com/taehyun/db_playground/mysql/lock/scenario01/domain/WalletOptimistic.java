package com.taehyun.db_playground.mysql.lock.scenario01.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(name = "wallets_optimistic_v1")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class WalletOptimistic {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private Long userId;

    private Long balance;

    @Version
    private Long version;

    private WalletOptimistic(Long userId, Long balance) {
        this.userId = userId;
        this.balance = balance;
    }

    public static WalletOptimistic createWalletOptimistic(Long userId, Long balance){
        return new WalletOptimistic(userId, balance);
    }

    public void deduct(Long amount) {

        if (amount == null)
            throw new IllegalArgumentException("null 일 수 없습니다");

        if (amount == 0)
            throw new IllegalArgumentException("차감할 금액이 0 일 수 없습니다");

        if (this.balance < amount)
            throw new IllegalArgumentException("잔고가 부족합니다");

        this.balance -= amount;
    }
}
