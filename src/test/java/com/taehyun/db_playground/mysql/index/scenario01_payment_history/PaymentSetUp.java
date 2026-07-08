package com.taehyun.db_playground.mysql.index.scenario01_payment_history;

import com.taehyun.db_playground.mysql.index.scenario01_payment_history.domain.PaymentType;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;

import java.sql.PreparedStatement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.concurrent.ThreadLocalRandom;

public final class PaymentSetUp {

    private static final int TOTAL_DATA_SIZE = 1_000_000;
    private static final int BATCH_SIZE = 10_000;

    public static void setUp(JdbcTemplate jdbcTemplate) {

        Integer count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM payments_v1", Integer.class);

        if (count != null && count > 0)
            return;

        String sql = "INSERT INTO payments_v1 (user_id, amount, payment_type, payment_status, created_at) VALUES (?, ?, ?, ?, ?)";

        for (int i = 0; i < TOTAL_DATA_SIZE; i += BATCH_SIZE) {

            final int currentBatchSize = Math.min(BATCH_SIZE, TOTAL_DATA_SIZE - i);

            jdbcTemplate.batchUpdate(sql, new BatchPreparedStatementSetter() {
                @Override
                public void setValues(PreparedStatement ps, int index) throws java.sql.SQLException {
                    ps.setLong(1, ThreadLocalRandom.current().nextLong(1, 50_001));
                    ps.setLong(2, ThreadLocalRandom.current().nextLong(10, 5001) * 100);
                    ps.setString(3, getRandomType().name());
                    ps.setString(4, getRandomStatus());
                    ps.setTimestamp(5, Timestamp.valueOf(getRandomDateTime()));
                }

                @Override
                public int getBatchSize() {
                    return currentBatchSize;
                }
            });
        }
    }

    private static PaymentType getRandomType() {
        int rand = ThreadLocalRandom.current().nextInt(100);

        if (rand < 70) return PaymentType.CARD;
        if (rand < 90) return PaymentType.TRANSFER;
        return PaymentType.PAY;
    }

    private static String getRandomStatus() {
        int rand = ThreadLocalRandom.current().nextInt(100);

        if (rand < 85) return "SUCCESS";
        if (rand < 95) return "FAIL";
        return "CANCEL";
    }

    private static LocalDateTime getRandomDateTime() {
        long minDay = LocalDateTime.now().minusMonths(6).toEpochSecond(java.time.ZoneOffset.UTC);
        long maxDay = LocalDateTime.now().toEpochSecond(java.time.ZoneOffset.UTC);
        long randomDay = ThreadLocalRandom.current().nextLong(minDay, maxDay);

        return LocalDateTime.ofEpochSecond(randomDay, 0, java.time.ZoneOffset.UTC);
    }

}