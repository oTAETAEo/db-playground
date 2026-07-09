package com.taehyun.db_playground.mysql.lock.scenario01.repository;

import com.taehyun.db_playground.mysql.lock.scenario01.domain.WalletNoLock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface WalletNoLockRepository extends JpaRepository<WalletNoLock, Long> {

    Optional<WalletNoLock> findByUserId(Long userId);

}
