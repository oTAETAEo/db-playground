package com.taehyun.db_playground.mysql.lock.scenario01.repository;

import com.taehyun.db_playground.mysql.lock.scenario01.domain.WalletOptimistic;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface WalletOptimisticRepository extends JpaRepository<WalletOptimistic, Long> {

    Optional<WalletOptimistic> findByUserId(Long userId);

}
