package com.taehyun.db_playground.mysql.lock.scenario01.repository;

import com.taehyun.db_playground.mysql.lock.scenario01.domain.WalletPessimistic;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface WalletPessimisticRepository extends JpaRepository<WalletPessimistic, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<WalletPessimistic> findByUserId(Long userId);

}
