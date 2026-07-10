package com.taehyun.db_playground.redis.lock.scenario01.interceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.time.Duration;

@Component("idempotencyInterceptor")
@RequiredArgsConstructor
public class IdempotencyInterceptor implements HandlerInterceptor {

    private final StringRedisTemplate stringRedisTemplate;

    public static final String REDIS_KEY_PREFIX = "idempotency:order:";

    @Override
    public boolean preHandle(HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull Object handler) throws Exception {
        String idempotencyKey = request.getHeader("Idempotency-Key");

        // 멱등성 헤더가 없는 요청은 검증을 패스시킵니다.
        if (idempotencyKey == null || idempotencyKey.isBlank()) {
            return true;
        }

        String redisKey = REDIS_KEY_PREFIX + idempotencyKey;

        // 1차 방어 : SET NX 명령어로 레디스 분산 락 선점 시도 (2분 TTL)
        Boolean isFirstRequest = stringRedisTemplate.opsForValue()
                .setIfAbsent(redisKey, "PROCESSING", Duration.ofMinutes(2));

        // 이미 키가 존재한다면 예외 발생
        if (Boolean.FALSE.equals(isFirstRequest)) {
            throw new IllegalStateException("이미 처리 중이거나 완료된 요청입니다. (최전방 멱등성 방어)");
        }

        // 첫 요청은 컨트롤러로 진입.
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull Object handler, Exception ex) throws Exception {

        String idempotencyKey = request.getHeader("Idempotency-Key");
        if (idempotencyKey == null || idempotencyKey.isBlank()) {
            return;
        }

        String redisKey = REDIS_KEY_PREFIX + idempotencyKey;
        if (ex != null) {

            stringRedisTemplate.delete(redisKey);
        } else {

            stringRedisTemplate.opsForValue().set(redisKey, "SUCCESS", Duration.ofMinutes(2));
        }
    }
}