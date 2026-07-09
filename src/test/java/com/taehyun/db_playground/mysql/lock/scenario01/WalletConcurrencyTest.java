package com.taehyun.db_playground.mysql.lock.scenario01;

import com.taehyun.db_playground.mysql.lock.scenario01.domain.*;
import com.taehyun.db_playground.mysql.lock.scenario01.repository.PaymentHistoryRepository;
import com.taehyun.db_playground.mysql.lock.scenario01.repository.WalletOptimisticRepository;
import com.taehyun.db_playground.mysql.lock.scenario01.repository.WalletNoLockRepository;
import com.taehyun.db_playground.mysql.lock.scenario01.repository.WalletPessimisticRepository;
import com.taehyun.db_playground.mysql.lock.scenario01.service.PurchaseService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.orm.ObjectOptimisticLockingFailureException;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class WalletConcurrencyTest {

    @Autowired
    private PurchaseService purchaseService;

    @Autowired
    private WalletNoLockRepository walletNoLockRepository;

    @Autowired
    private WalletOptimisticRepository walletOptimisticRepository;

    @Autowired
    private WalletPessimisticRepository walletPessimisticRepository;

    @Autowired
    private PaymentHistoryRepository paymentHistoryRepository;

    @BeforeEach
    void setUp() {

        Long userId = 1L;
        Long balance = 10000L;

        walletNoLockRepository.deleteAllInBatch();
        walletOptimisticRepository.deleteAllInBatch();
        walletPessimisticRepository.deleteAllInBatch();

        paymentHistoryRepository.deleteAllInBatch();

        walletNoLockRepository.save(WalletNoLock.createWalletNoLock(userId, balance));
        walletOptimisticRepository.save(WalletOptimistic.createWalletOptimistic(userId, balance));
        walletPessimisticRepository.save(WalletPessimistic.createWalletPessimistic(userId, balance));
    }

    @Test
    @DisplayName("Case 1: 아무런 락이 없을 때 동시성 요청 시 데이터 정합성이 깨진다.")
    void concurrencyTest_NoLock() {

        // given
        int threadCount = 100;

        Long targetUserId = 1L;
        String targetItemId = "Chicken";
        long deductAmount = 100L;

        // when
        try (
                ExecutorService executorService = Executors.newFixedThreadPool(32)
        ) {
            List<CompletableFuture<Void>> futures = IntStream.range(0, threadCount)
                    .mapToObj(i -> CompletableFuture.runAsync(() -> {

                        purchaseService.purchaseNoLock(targetUserId, deductAmount, targetItemId);

                    }, executorService))
                    .toList();

            CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
        }

        // then
        WalletNoLock walletNoLock = walletNoLockRepository.findByUserId(targetUserId).orElseThrow();

        long historyCount = paymentHistoryRepository.countByLockType(LockType.NO_LOCK);

        assertThat(walletNoLock.getBalance()).isGreaterThan(0L);

        System.out.println("======================================");
        System.out.println("No Lock 정합성 테스트 결과");
        System.out.println("생성된 총 주문 내역 수: " + historyCount + "건");
        System.out.println("최종 남은 DB 실제 잔액: " + walletNoLock.getBalance() + "원");
        System.out.println("지갑에서 실제로 차감된 총 금액: " + (10000L - walletNoLock.getBalance()) + "원");
        System.out.println("======================================");

        assertThat(historyCount).isEqualTo(100);
        assertThat(10000L - walletNoLock.getBalance()).isNotEqualTo(10000L);
    }

    @Test
    @DisplayName("Case 2: 낙관적 락을 적용하면 충돌 시 예외가 발생하여 데이터 오염은 막지만, 대다수의 요청이 실패한다.")
    void concurrencyTest_OptimisticLock() {

        // given
        int threadCount = 100;

        Long targetUserId = 1L;
        String targetItemId = "Chicken";
        long deductAmount = 100L;

        AtomicInteger optimisticLockExceptionCount = new AtomicInteger(0);

        // when
        try (
                ExecutorService executorService = Executors.newFixedThreadPool(32)
        ) {
            List<CompletableFuture<Void>> futures = IntStream.range(0, threadCount)
                    .mapToObj(i -> CompletableFuture.runAsync(() -> {
                        try {

                            purchaseService.purchaseOptimistic(targetUserId, deductAmount, targetItemId);

                        } catch (ObjectOptimisticLockingFailureException e) {

                            optimisticLockExceptionCount.incrementAndGet();

                        }
                    }, executorService))
                    .toList();

            CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
        }

        // then
        WalletOptimistic walletOptimistic = walletOptimisticRepository.findByUserId(targetUserId)
                .orElseThrow();

        long historyCount = paymentHistoryRepository.countByLockType(LockType.OPTIMISTIC);

        System.out.println("======================================");
        System.out.println("낙관적 락 정합성 테스트 결과");
        System.out.println("생성된 총 주문 내역 수: " + historyCount + "건");
        System.out.println("최종 남은 DB 실제 잔액: " + walletOptimistic.getBalance() + "원");
        System.out.println("지갑에서 실제로 차감된 총 금액: " + (10000L - walletOptimistic.getBalance()) + "원");
        System.out.println("낙관적 락 실패 횟수: " + optimisticLockExceptionCount.get() + "번");
        System.out.println("======================================");

        long expectedDeductedAmount = historyCount * deductAmount;
        assertThat(10000L - walletOptimistic.getBalance()).isEqualTo(expectedDeductedAmount);

        assertThat(walletOptimistic.getVersion() + optimisticLockExceptionCount.get()).isEqualTo(threadCount);
    }

    @Test
    @DisplayName("Case 3: 비관적 락 적용 시 - 스레드들이 줄을 서서 순차 처리되므로 에러 없이 잔액이 정확히 0원이 된다.")
    void concurrencyTest_PessimisticLock() {

        // given
        int threadCount = 100;

        Long targetUserId = 1L;
        String targetItemId = "Chicken";
        long deductAmount = 100L;

        // when
        try (ExecutorService executorService = Executors.newFixedThreadPool(32)) {
            List<CompletableFuture<Void>> futures = IntStream.range(0, threadCount)
                    .mapToObj(i -> CompletableFuture.runAsync(() -> {

                        purchaseService.purchasePessimistic(targetUserId, deductAmount, targetItemId);

                    }, executorService))
                    .toList();
            CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
        }

        // then
        WalletPessimistic walletPessimistic = walletPessimisticRepository.findByUserId(targetUserId)
                .orElseThrow();

        long historyCount = paymentHistoryRepository.countByLockType(LockType.PESSIMISTIC);

        System.out.println("======================================");
        System.out.println("비관적 락 정합성 테스트 결과");
        System.out.println("생성된 총 주문 내역 수: " + historyCount + "건");
        System.out.println("최종 남은 DB 실제 잔액: " + walletPessimistic.getBalance() + "원");
        System.out.println("지갑에서 실제로 차감된 총 금액: " + (10000L - walletPessimistic.getBalance()) + "원");
        System.out.println("======================================");


        assertThat(walletPessimistic.getBalance()).isEqualTo(0L);
        assertThat(historyCount).isEqualTo(100);
    }

}
