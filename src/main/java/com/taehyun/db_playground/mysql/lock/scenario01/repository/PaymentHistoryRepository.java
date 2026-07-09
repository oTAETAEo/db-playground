package com.taehyun.db_playground.mysql.lock.scenario01.repository;

import com.taehyun.db_playground.mysql.lock.scenario01.domain.PaymentHistory;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentHistoryRepository extends JpaRepository<PaymentHistory, Long> {

}
