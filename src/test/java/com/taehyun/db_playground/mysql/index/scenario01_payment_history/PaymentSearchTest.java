package com.taehyun.db_playground.mysql.index.scenario01_payment_history;

import com.taehyun.db_playground.mysql.index.scenario01_payment_history.domain.Payment;
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
import java.util.Comparator;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

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
    void measurePaymentSearchPerformance_1() {

        Long targetUserId = 1L;
        LocalDateTime searchStartAt = LocalDateTime.of(2026, 3, 1, 0, 0);
        LocalDateTime searchEndAt = LocalDateTime.of(2026, 6, 1, 0, 0);

        StopWatch stopWatch = new StopWatch("결제 조회 성능 테스트");
        stopWatch.start("대상 쿼리 실행 (인덱스 없음)");

        var results = paymentSearchService.getSuccessPaymentsByPeriod(targetUserId, PaymentType.CARD, searchStartAt ,searchEndAt);

        stopWatch.stop();

        assertThat(results).isNotEmpty();

        assertThat(results).allSatisfy(payment -> {
            assertThat(payment.getUserId()).isEqualTo(targetUserId);
            assertThat(payment.getPaymentType()).isEqualTo(PaymentType.CARD);
            assertThat(payment.getPaymentStatus()).isEqualTo("SUCCESS");
            assertThat(payment.getCreatedAt()).isAfterOrEqualTo(searchStartAt);
            assertThat(payment.getCreatedAt()).isBeforeOrEqualTo(searchEndAt);
        });

        assertThat(results).isSortedAccordingTo(
                Comparator.comparing(Payment::getCreatedAt).reversed()
        );

        measurementResultsPrint(results, stopWatch, "Case 1: No Index 실험 결과");
    }

    @Test
    @DisplayName("Case 2: user_id 단일 인덱스 있는 상태에서 특정 유저의 최근 3달간 성공한 카드 결제 내역 조회")
    void measurePaymentSearchPerformance_2() {

        Long targetUserId = 1L;
        LocalDateTime searchStartAt = LocalDateTime.of(2026, 3, 1, 0, 0);
        LocalDateTime searchEndAt = LocalDateTime.of(2026, 6, 1, 0, 0);

        StopWatch stopWatch = new StopWatch("결제 조회 성능 테스트");
        stopWatch.start("대상 쿼리 실행 (user_id 단일 인덱스)");

        var results = paymentSearchService.getSuccessPaymentsByPeriod(targetUserId, PaymentType.CARD, searchStartAt ,searchEndAt);

        stopWatch.stop();

        assertThat(results).isNotEmpty();

        assertThat(results).allSatisfy(payment -> {
            assertThat(payment.getUserId()).isEqualTo(targetUserId);
            assertThat(payment.getPaymentType()).isEqualTo(PaymentType.CARD);
            assertThat(payment.getPaymentStatus()).isEqualTo("SUCCESS");
            assertThat(payment.getCreatedAt()).isAfterOrEqualTo(searchStartAt);
            assertThat(payment.getCreatedAt()).isBeforeOrEqualTo(searchEndAt);
        });

        assertThat(results).isSortedAccordingTo(
                Comparator.comparing(Payment::getCreatedAt).reversed()
        );

        measurementResultsPrint(results, stopWatch, "Case 2: user_id 단일 인덱스 실험 결과");
    }

    @Test
    @DisplayName("Case 3: 복합 인덱스 있는 상태에서 특정 유저의 최근 3달간 성공한 카드 결제 내역 조회")
    void measurePaymentSearchPerformance_3() {

        Long targetUserId = 1L;
        LocalDateTime searchStartAt = LocalDateTime.of(2026, 3, 1, 0, 0);
        LocalDateTime searchEndAt = LocalDateTime.of(2026, 6, 1, 0, 0);

        StopWatch stopWatch = new StopWatch("결제 조회 성능 테스트");
        stopWatch.start("대상 쿼리 실행 복합 인덱스 (user_id, payment_type, payment_status, created_at)");

        var results = paymentSearchService.getSuccessPaymentsByPeriod(targetUserId, PaymentType.CARD, searchStartAt ,searchEndAt);

        stopWatch.stop();

        assertThat(results).isNotEmpty();

        assertThat(results).allSatisfy(payment -> {
            assertThat(payment.getUserId()).isEqualTo(targetUserId);
            assertThat(payment.getPaymentType()).isEqualTo(PaymentType.CARD);
            assertThat(payment.getPaymentStatus()).isEqualTo("SUCCESS");
            assertThat(payment.getCreatedAt()).isAfterOrEqualTo(searchStartAt);
            assertThat(payment.getCreatedAt()).isBeforeOrEqualTo(searchEndAt);
        });

        assertThat(results).isSortedAccordingTo(
                Comparator.comparing(Payment::getCreatedAt).reversed()
        );

        measurementResultsPrint(results, stopWatch, "Case 3: 복합 인덱스 실험 결과");
    }

    private void measurementResultsPrint(List<Payment> results, StopWatch stopWatch, String str) {
        System.out.println("\n====== " + str + " ======");
        System.out.println("조회된 데이터 건수: " + results.size() + " 건");
        System.out.println("총 소요 시간(ms): " + stopWatch.getTotalTimeMillis() + "ms");
        System.out.println("=========================================\n");
    }

}
