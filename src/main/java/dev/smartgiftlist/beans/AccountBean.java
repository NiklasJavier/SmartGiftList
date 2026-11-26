package dev.smartgiftlist.beans;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

import dev.smartgiftlist.jdbc.NoConnectionException;
import dev.smartgiftlist.jdbc.PostgreSQLAccess;
import dev.smartgiftlist.security.PasswordUtil;

/**
 * Session bean handling user registration and account checks.
 */
public class AccountBean {

    private int userID;
    private String username;
    private String email;
    private String password;
    private String confirm_password;
    private Date birthday;
    private boolean isAdmin;
    private String icon;
    private Date joinDate;
    Connection dbConn;

    public AccountBean() throws NoConnectionException {
        this(true);
    }

    protected AccountBean(boolean connectDatabase) throws NoConnectionException {
        super();
        System.out.println("[AccountBean] Initializing AccountBean");
        this.username = "";
        this.email = "";
        this.password = "";
        this.confirm_password = "";
        this.birthday = null;
        this.isAdmin = false;
        this.icon = "Lumibella";
        this.joinDate = initJoinDate();
        if (connectDatabase) {
            this.dbConn = new PostgreSQLAccess().getConnection();
        }
    }

    public int getUserID() {
        return userID;
    }

    public void setUserID(int userID) {
        this.userID = userID;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username != null ? username : "";
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email != null ? email : "";
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getConfirm_password() {
        return confirm_password;
    }

    public void setConfirm_password(String confirm_password) {
        this.confirm_password = confirm_password;
    }

    public boolean comparePasswords() {
        return this.password != null
                && !this.password.isEmpty()
                && this.password.equals(this.confirm_password);
    }

    public Date getBirthday() {
        return birthday;
    }

    public void setBirthday(Date birthday) {
        this.birthday = birthday;
    }

    public void setStringToSqlDate(String dateString) {
        if (dateString == null || dateString.isBlank()) {
            this.birthday = null;
            return;
        }
        try {
            LocalDate localDate = LocalDate.parse(dateString.trim(), DateTimeFormatter.ISO_LOCAL_DATE);
            this.birthday = Date.valueOf(localDate);
        } catch (DateTimeParseException ex) {
            this.birthday = null;
        }
    }

    public boolean isAdmin() {
        return isAdmin;
    }

    public void setAdmin(boolean isAdmin) {
        this.isAdmin = isAdmin;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public Date getJoinDate() {
        return joinDate;
    }

    public void setJoinDate(Date joinDate) {
        this.joinDate = joinDate;
    }

    public Date initJoinDate() {
        Date currentDate = new Date(System.currentTimeMillis());
        this.joinDate = currentDate;
        return currentDate;
    }

    public void insertAccount() throws NoConnectionException, SQLException {
        String sql = "INSERT INTO Users (Username, Email, Password, Birthday, IsAdmin, Icon, JoinDate) VALUES (?, ?, ?, ?, ?, ?, ?)";
        System.out.println("[DB] " + sql);
        String hashedPassword = PasswordUtil.hashPassword(this.password);
        try (PreparedStatement prep = this.dbConn.prepareStatement(sql)) {
            prep.setString(1, this.username);
            prep.setString(2, this.email);
            prep.setString(3, hashedPassword);
            prep.setDate(4, this.birthday);
            prep.setBoolean(5, this.isAdmin);
            prep.setString(6, this.icon);
            prep.setDate(7, this.joinDate);
            prep.executeUpdate();
            System.out.println("[Account] Created user account for '" + this.username + "'");
        }
    }

    public boolean checkUsernameExist() throws NoConnectionException, SQLException {
        String sql = "SELECT Username FROM Users WHERE Username = ?";
        System.out.println("[DB] " + sql);
        try (PreparedStatement prep = this.dbConn.prepareStatement(sql)) {
            prep.setString(1, this.username);
            try (ResultSet rs = prep.executeQuery()) {
                return rs.next();
            }
        }
    }

    public boolean checkEmailExist() throws NoConnectionException, SQLException {
        String sql = "SELECT Email FROM Users WHERE Email = ?";
        System.out.println("[DB] " + sql);
        try (PreparedStatement prep = this.dbConn.prepareStatement(sql)) {
            prep.setString(1, this.email);
            try (ResultSet rs = prep.executeQuery()) {
                return rs.next();
            }
        }
    }

    public boolean insertAccountIfUsernameEmailNotExists() throws SQLException {
        if (!this.checkUsernameExist() && !this.checkEmailExist()) {
            this.insertAccount();
            return true;
        }
        return false;
    }
}
