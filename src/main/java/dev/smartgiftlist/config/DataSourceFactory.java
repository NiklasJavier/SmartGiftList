package dev.smartgiftlist.config;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Builds a bounded HikariCP DataSource with startup retry and schema search_path binding.
 */
public final class DataSourceFactory {

    private static final Logger LOG = LoggerFactory.getLogger(DataSourceFactory.class);
    private static final int MAX_ATTEMPTS = 5;
    private static final long RETRY_DELAY_MS = 1_000L;
    private static final int MAX_POOL_SIZE = 10;
    private static final int MIN_IDLE = 1;
    private static final long CONNECTION_TIMEOUT_MS = 10_000L;

    private DataSourceFactory() {
    }

    public static HikariDataSource create(DatabaseConfig config) {
        SQLException lastFailure = null;
        for (int attempt = 1; attempt <= MAX_ATTEMPTS; attempt++) {
            HikariDataSource dataSource = null;
            try {
                dataSource = new HikariDataSource(buildHikariConfig(config));
                try (Connection conn = dataSource.getConnection();
                     Statement stmt = conn.createStatement()) {
                    stmt.execute("SELECT 1");
                }
                LOG.info("Connected HikariCP pool to {} (schema={})", config.getJdbcUrl(), config.getSchema());
                return dataSource;
            } catch (RuntimeException | SQLException ex) {
                if (dataSource != null) {
                    dataSource.close();
                }
                lastFailure = ex instanceof SQLException sqlEx ? sqlEx : new SQLException(ex);
                LOG.warn("Database connection attempt {}/{} failed: {}", attempt, MAX_ATTEMPTS, ex.getMessage());
                if (attempt < MAX_ATTEMPTS) {
                    try {
                        Thread.sleep(RETRY_DELAY_MS);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                }
            }
        }
        throw new IllegalStateException("Unable to connect to PostgreSQL after " + MAX_ATTEMPTS + " attempts", lastFailure);
    }

    private static HikariConfig buildHikariConfig(DatabaseConfig config) {
        HikariConfig hikari = new HikariConfig();
        hikari.setPoolName("smartgiftlist-pool");
        hikari.setDriverClassName("org.postgresql.Driver");
        hikari.setJdbcUrl(config.getJdbcUrl());
        hikari.setUsername(config.getUsername());
        hikari.setPassword(config.getPassword());
        hikari.setMaximumPoolSize(MAX_POOL_SIZE);
        hikari.setMinimumIdle(MIN_IDLE);
        hikari.setConnectionTimeout(CONNECTION_TIMEOUT_MS);
        hikari.setConnectionInitSql("SET search_path TO " + config.getSchema());
        return hikari;
    }
}
