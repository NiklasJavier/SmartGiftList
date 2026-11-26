package dev.smartgiftlist.jdbc;

import java.sql.SQLException;

@SuppressWarnings("serial")
public class NoConnectionException extends SQLException {

    public NoConnectionException() {
        super("[DB] Failed to establish JDBC connection");
    }

    public NoConnectionException(String msg) {
        super(msg);
    }
}
