package dev.smartgiftlist.beans;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import dev.smartgiftlist.jdbc.NoConnectionException;
import dev.smartgiftlist.jdbc.PostgreSQLAccess;
import dev.smartgiftlist.security.PasswordUtil;

/**
 * Session bean handling user authentication against PBKDF2 password hashes.
 */
public class LoginBean {

    String username;
    String password;
    boolean loggedIn;
    Connection dbConn;

    public LoginBean() throws NoConnectionException {
        this(true);
    }

    protected LoginBean(boolean connectDatabase) throws NoConnectionException {
        this.username = "";
        this.password = "";
        this.loggedIn = false;
        if (connectDatabase) {
            this.dbConn = new PostgreSQLAccess().getConnection();
        }
    }

    public boolean checkUsernamePassword() throws SQLException {
        String sql = "SELECT Password FROM Users WHERE Username = ?";
        System.out.println("[DB] " + sql);
        try (PreparedStatement prep = this.dbConn.prepareStatement(sql)) {
            prep.setString(1, this.username);
            try (ResultSet rs = prep.executeQuery()) {
                if (rs.next()) {
                    String storedHash = rs.getString("Password");
                    return PasswordUtil.verifyPassword(this.password, storedHash);
                }
            }
        } catch (SQLException se) {
            se.printStackTrace();
            throw se;
        }
        return false;
    }

    public boolean isLoggedIn() {
        return loggedIn;
    }

    public void setLoggedIn(boolean loggedIn) {
        this.loggedIn = loggedIn;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
