package com.taehyun.db_playground.redis.lock.scenario01.repositroy;

import com.taehyun.db_playground.redis.lock.scenario01.domain.PaymentTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PaymentTransactionRepository extends JpaRepository<PaymentTransaction, Long> {
}
