package com.taehyun.db_playground.mysql.lock.scenario01.repository;

import com.taehyun.db_playground.mysql.lock.scenario01.domain.WalletPessimistic;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface WalletPessimisticRepository extends JpaRepository<WalletPessimistic, Long> {


    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select w from WalletPessimistic w where w.userId = :userId")
    Optional<WalletPessimistic> findByUserIdWithLock(@Param("userId") Long userId);

    Optional<WalletPessimistic> findByUserId(Long userId);
}
