package com.taehyun.db_playground.mysql.index.scenario01_payment_history;

import com.taehyun.db_playground.mysql.index.scenario01_payment_history.domain.PaymentType;
import com.taehyun.db_playground.mysql.index.scenario01_payment_history.service.PaymentSearchService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.util.StopWatch;

import java.time.LocalDateTime;

@SpringBootTest
class PaymentSearchTest {

    @Autowired
    private PaymentSearchService paymentSearchService;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void setUp() {
        PaymentSetUp.setUp(jdbcTemplate);
    }

    @Test
    @DisplayName("Case 1: 인덱스 없는 상태에서 특정 유저의 최근 3달간 성공한 카드 결제 내역 조회")
    void measurePaymentSearchPerformance() {

        Long targetUserId = 1L;
        LocalDateTime searchStartAt = LocalDateTime.of(2026, 3, 1, 0, 0);
        LocalDateTime searchEndAt = LocalDateTime.of(2026, 6, 1, 0, 0);

        StopWatch stopWatch = new StopWatch("결제 조회 성능 테스트");
        stopWatch.start("대상 쿼리 실행 (인덱스 없음)");

        var results = paymentSearchService.getPaymentsByPeriod(targetUserId, PaymentType.CARD, searchStartAt ,searchEndAt);

        stopWatch.stop();

        System.out.println("\n====== Case 1: No Index 실험 결과 ======");
        System.out.println("조회된 데이터 건수: " + results.size() + " 건");
        System.out.println("총 소요 시간(ms): " + stopWatch.getTotalTimeMillis() + "ms");
        System.out.println("=========================================\n");
    }
}