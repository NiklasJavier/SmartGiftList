package dev.smartgiftlist.jdbc;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Base JDBC connection helper with bounded startup retry and search_path schema initialization.
 */
public abstract class JDBCAccess {

    private static final int MAX_CONNECT_ATTEMPTS = 5;
    private static final long RETRY_DELAY_MS = 1_000L;

    Connection dbConn;
    String dbDrivername;
    String dbURL;
    String dbUser;
    String dbPassword;
    String dbSchema;

    public JDBCAccess() throws NoConnectionException {
        this(true);
    }

    protected JDBCAccess(boolean connectImmediately) throws NoConnectionException {
        this.setDBParms();
        if (connectImmediately) {
            this.createConnection();
        }
    }

    public abstract void setDBParms();

    public void setSchema() throws NoConnectionException {
        try {
            String safeSchema = PostgreSQLAccess.validateSchemaIdentifier(dbSchema);
            String sql = "SET search_path TO " + safeSchema;
            System.out.println("[DB] " + sql);
            try (Statement stmt = dbConn.createStatement()) {
                stmt.executeUpdate(sql);
            }
            System.out.println("[DB] Schema set to '" + safeSchema + "'");
        } catch (SQLException se) {
            se.printStackTrace();
            throw new NoConnectionException();
        }
    }

    public void createConnection() throws NoConnectionException {
        try {
            Class.forName(dbDrivername);
            System.out.println("[DB] JDBC driver loaded");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            throw new NoConnectionException();
        }

        SQLException lastException = null;
        for (int attempt = 1; attempt <= MAX_CONNECT_ATTEMPTS; attempt++) {
            try {
                dbConn = DriverManager.getConnection(dbURL, dbUser, dbPassword);
                System.out.println("[DB] Database connection established");
                return;
            } catch (SQLException e) {
                lastException = e;
                System.err.println("[DB] Connection attempt " + attempt + "/" + MAX_CONNECT_ATTEMPTS + " failed: " + e.getMessage());
                if (attempt < MAX_CONNECT_ATTEMPTS) {
                    try {
                        Thread.sleep(RETRY_DELAY_MS);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                }
            }
        }
        if (lastException != null) {
            lastException.printStackTrace();
        }
        throw new NoConnectionException();
    }

    public Connection getConnection() throws NoConnectionException {
        this.setSchema();
        return dbConn;
    }
}
