package com.taehyun.db_playground.redis.lock.scenario01.repositroy;

import com.taehyun.db_playground.redis.lock.scenario01.domain.Wallet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository("idempotencyWalletRepository")
public interface WalletRepository extends JpaRepository<Wallet, Long> {

    Optional<Wallet> findByUserId(Long userId);

}
