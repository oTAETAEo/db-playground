package com.taehyun.db_playground.redis.lock.scenario01;

import com.taehyun.db_playground.redis.lock.scenario01.domain.PaymentTransaction;
import com.taehyun.db_playground.redis.lock.scenario01.domain.Wallet;
import com.taehyun.db_playground.redis.lock.scenario01.repositroy.PaymentTransactionRepository;
import com.taehyun.db_playground.redis.lock.scenario01.repositroy.WalletRepository;
import com.taehyun.db_playground.redis.lock.scenario01.service.PaymentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class PaymentServiceTest {

    @Autowired
    private PaymentService paymentService;

    @Autowired
    private WalletRepository walletRepository;

    @Autowired
    private PaymentTransactionRepository paymentTransactionRepository;

    @BeforeEach
    void setUp() {
        paymentTransactionRepository.deleteAllInBatch();
        walletRepository.deleteAllInBatch();
    }

    @DisplayName("결제 테스트: 단일 요청이 들어오면 지갑 잔액이 차감되고 영수증이 정확히 발행되어야 한다")
    @Test
    void paymentCompleted() {
        // given
        Long userId = 1L;
        long initialBalance = 10000L;
        long paymentAmount = 5000L;
        String idempotencyKey = "idempotency:order:xxxxxxxx";

        // 최초 지갑 잔액 10,000원 세팅
        walletRepository.save(Wallet.createWallet(userId, initialBalance));

        // when
        Long savePaymentTransactionId = paymentService.process(userId, paymentAmount, idempotencyKey);

        // then
        PaymentTransaction transaction = paymentTransactionRepository.findById(savePaymentTransactionId)
                .orElseThrow(() -> new AssertionError("결제 트랜잭션 영수증이 생성되지 않았습니다."));

        assertThat(transaction.getUserId()).isEqualTo(userId);
        assertThat(transaction.getTotalAmount()).isEqualTo(paymentAmount);
        assertThat(transaction.getIdempotencyKey()).isEqualTo(idempotencyKey);
        assertThat(transaction.getCreatedAt()).isNotNull();

        Wallet wallet = walletRepository.findByUserId(userId)
                .orElseThrow(() -> new AssertionError("유저의 지갑을 찾을 수 없습니다."));

        long expectedBalance = initialBalance - paymentAmount;
        assertThat(wallet.getBalance()).isEqualTo(expectedBalance);
    }
}