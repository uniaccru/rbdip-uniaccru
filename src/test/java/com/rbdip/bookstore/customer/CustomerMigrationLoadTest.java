package com.rbdip.bookstore.customer;

import static org.assertj.core.api.Assertions.assertThat;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.Test;
import org.postgresql.ds.PGSimpleDataSource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@Testcontainers
class CustomerMigrationLoadTest {

    private static final int READERS = 4;

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine")
            .withDatabaseName("bookstore_customer_migration")
            .withUsername("bookstore")
            .withPassword("bookstore");

    @Test
    void oldCustomerReadersSurviveExpandAndBackfill() throws Exception {
        PGSimpleDataSource dataSource = new PGSimpleDataSource();
        dataSource.setUrl(postgres.getJdbcUrl());
        dataSource.setUser(postgres.getUsername());
        dataSource.setPassword(postgres.getPassword());
        JdbcTemplate jdbc = new JdbcTemplate(dataSource);

        flyway(dataSource, "2").migrate();
        jdbc.update("INSERT INTO customers (full_name) VALUES ('Иванов Иван')");

        List<Exception> errors = new CopyOnWriteArrayList<>();
        AtomicBoolean keepReading = new AtomicBoolean(true);
        AtomicInteger reads = new AtomicInteger();
        CountDownLatch started = new CountDownLatch(READERS);
        ExecutorService readers = Executors.newFixedThreadPool(READERS);
        for (int i = 0; i < READERS; i++) {
            readers.submit(() -> readLegacyColumn(dataSource, keepReading, reads, errors, started));
        }
        assertThat(started.await(5, TimeUnit.SECONDS)).isTrue();

        flyway(dataSource, "4").migrate();

        keepReading.set(false);
        readers.shutdown();
        assertThat(readers.awaitTermination(5, TimeUnit.SECONDS)).isTrue();
        assertThat(errors).isEmpty();
        assertThat(reads).hasValueGreaterThan(0);
        assertThat(jdbc.queryForObject(
                "SELECT count(*) FROM customers WHERE first_name = 'Иванов' AND last_name = 'Иван'",
                Integer.class)).isEqualTo(1);
    }

    private void readLegacyColumn(
            PGSimpleDataSource dataSource,
            AtomicBoolean keepReading,
            AtomicInteger reads,
            List<Exception> errors,
            CountDownLatch started) {
        started.countDown();
        while (keepReading.get()) {
            try (Connection connection = dataSource.getConnection();
                    Statement statement = connection.createStatement()) {
                statement.executeQuery("SELECT full_name FROM customers LIMIT 1");
                reads.incrementAndGet();
            } catch (SQLException exception) {
                errors.add(exception);
            }
        }
    }

    private static Flyway flyway(PGSimpleDataSource dataSource, String target) {
        return Flyway.configure()
                .dataSource(dataSource)
                .locations("classpath:db/migration")
                .target(target)
                .load();
    }
}
