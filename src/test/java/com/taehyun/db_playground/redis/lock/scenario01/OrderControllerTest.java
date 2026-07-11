package com.taehyun.db_playground.redis.lock.scenario01;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.taehyun.db_playground.redis.lock.scenario01.controller.OrderController;
import com.taehyun.db_playground.redis.lock.scenario01.domain.Wallet;
import com.taehyun.db_playground.redis.lock.scenario01.repositroy.PaymentTransactionRepository;
import com.taehyun.db_playground.redis.lock.scenario01.repositroy.WalletRepository;
import com.taehyun.db_playground.redis.lock.scenario01.service.OrderItem;
import com.taehyun.db_playground.redis.lock.scenario01.service.PaymentService;
import jakarta.servlet.ServletException;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;

import static com.taehyun.db_playground.redis.lock.scenario01.interceptor.IdempotencyInterceptor.REDIS_KEY_PREFIX;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

@Slf4j
@SpringBootTest
@AutoConfigureMockMvc
class OrderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private WalletRepository walletRepository;

    @Autowired
    private PaymentTransactionRepository paymentTransactionRepository;

    @Autowired
    private PaymentService paymentService;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    private static final String IDEMPOTENCY_KEY = "test-idempotency-key-uuid-1234";

    @BeforeEach
    void setUp() {
        paymentTransactionRepository.deleteAll();
        walletRepository.deleteAll();

        stringRedisTemplate.delete(REDIS_KEY_PREFIX + IDEMPOTENCY_KEY);
    }

    @DisplayName("Case1: 멱등성 테스트 100개의 요청이 동시에 인입되어도 결제는 딱 1번만 성공해야 한다")
    @Test
    void concurrentOrderTest() {
        // given
        Long userId = 1L;
        long initialBalance = 10000L;

        walletRepository.save(Wallet.createWallet(userId, initialBalance));

        OrderController.OrderRequest orderRequest = new OrderController.OrderRequest(
                userId,
                List.of(new OrderItem("BHC 치킨", 5000L, 1L))
        );

        int threadCount = 100;

        CountDownLatch readyLatch = new CountDownLatch(1);

        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failCount = new AtomicInteger(0);

        // when
        try (ExecutorService executorService = Executors.newFixedThreadPool(32)){

            List<CompletableFuture<Void>> futures = IntStream.range(0, threadCount)
                    .mapToObj(i -> CompletableFuture.runAsync(

                            sendOrderApi(readyLatch, successCount, failCount, orderRequest),

                            executorService))
                    .toList();

            readyLatch.countDown();

            CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
        }

        // then
        log.info("=== 실험 결과 ===");
        log.info("성공한 요청 수: {}", successCount.get());
        log.info("막아낸 요청 수: {}", failCount.get());

        assertThat(successCount.get()).isEqualTo(1);
        assertThat(failCount.get()).isEqualTo(99);

        Wallet wallet = walletRepository.findByUserId(userId).orElseThrow();
        assertThat(wallet.getBalance()).isEqualTo(5000L);

        long transactionCount = paymentTransactionRepository.count();
        assertThat(transactionCount).isEqualTo(1L);
    }

    @DisplayName("Case 2: 레디스가 뚫렸을 때, RDB 복합 유니크 키 검증")
    @Test
    void rdbUniqueConstraintTest() {

        // given
        Long userId = 1L;
        long initialBalance = 10000L;
        long paymentAmount = 5000L;
        String expiredIdempotencyKey = "expired-idempotency-key-uuid-9999";

        walletRepository.save(Wallet.createWallet(userId, initialBalance));

        int threadCount = 10;
        CountDownLatch readyLatch = new CountDownLatch(1);

        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failCount = new AtomicInteger(0);

        // when
        try (ExecutorService executorService = Executors.newFixedThreadPool(10)) {
            List<CompletableFuture<Void>> futures = IntStream.range(0, threadCount)
                    .mapToObj(i -> CompletableFuture.runAsync(() -> {
                        try {
                            readyLatch.await();

                            paymentService.process(userId, paymentAmount, expiredIdempotencyKey);

                            successCount.incrementAndGet();
                        } catch (Exception e) {

                            failCount.incrementAndGet();
                        }
                    }, executorService))
                    .toList();

            readyLatch.countDown();
            CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
        }

        // then
        log.info("=== Case2 실험 결과 ===");
        log.info("성공한 요청 수: {} ", successCount.get());
        log.info("RDB 복합 유니크 제한으로 막아낸 중복 요청 수: {}", failCount.get());

        assertThat(successCount.get()).isEqualTo(1);
        assertThat(failCount.get()).isEqualTo(threadCount - 1);

        Wallet wallet = walletRepository.findByUserId(userId).orElseThrow();
        assertThat(wallet.getBalance()).isEqualTo(5000L);

        long transactionCount = paymentTransactionRepository.count();
        assertThat(transactionCount).isEqualTo(1L);
    }

    @DisplayName("Case3: 비즈니스 로직 실패 시 레디스 멱등성 키가 해제되어, 잔고 충전 후 동일한 키로 재시도하면 성공해야 한다")
    @Test
    void idempotencyKeyReleaseOnFailureTest() throws Exception {

        // given
        Long userId = 1L;
        long initialBalance = 0L;

        walletRepository.save(Wallet.createWallet(userId, initialBalance));

        OrderController.OrderRequest orderRequest = new OrderController.OrderRequest(
                userId,
                List.of(new OrderItem("BHC 치킨", 5000L, 1L))
        );

        // when - then
        assertThatThrownBy(() ->
                mockMvc.perform(post("/order")
                        .header("Idempotency-Key", IDEMPOTENCY_KEY)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(orderRequest)))
        ).isInstanceOf(ServletException.class)
                .hasCauseInstanceOf(IllegalArgumentException.class);

        String redisValue = stringRedisTemplate.opsForValue().get(REDIS_KEY_PREFIX + IDEMPOTENCY_KEY);
        assertThat(redisValue).isNull();

        // given
        Wallet wallet = walletRepository.findByUserId(userId).orElseThrow();
        wallet.increaseBalance(10000L);
        walletRepository.saveAndFlush(wallet);

        // when
        mockMvc.perform(post("/order")
                .header("Idempotency-Key", IDEMPOTENCY_KEY)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(orderRequest)));

        // then
        Wallet finalWallet = walletRepository.findByUserId(userId).orElseThrow();
        assertThat(finalWallet.getBalance()).isEqualTo(5000L);

        long transactionCount = paymentTransactionRepository.count();
        assertThat(transactionCount).isEqualTo(1L);

        String finalRedisValue = stringRedisTemplate.opsForValue().get(REDIS_KEY_PREFIX + IDEMPOTENCY_KEY);
        assertThat(finalRedisValue).isEqualTo("SUCCESS");

    }

    private Runnable sendOrderApi(
            CountDownLatch readyLatch, AtomicInteger successCount, AtomicInteger failCount, OrderController.OrderRequest orderRequest){

        return () -> {
            try {
                readyLatch.await();

                mockMvc.perform(post("/order")
                        .header("Idempotency-Key", IDEMPOTENCY_KEY)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(orderRequest)));

                successCount.incrementAndGet();
            } catch (Exception e) {
                failCount.incrementAndGet();
            }
        };
    }

}