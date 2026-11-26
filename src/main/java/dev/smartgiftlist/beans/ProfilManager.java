package dev.smartgiftlist.beans;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import dev.smartgiftlist.jdbc.NoConnectionException;
import dev.smartgiftlist.security.PasswordUtil;

/**
 * Manages user profile updates, avatars, and account deletion.
 */
public class ProfilManager {

    private Connection dbConn;

    public ProfilManager() {
    }

    public ProfilManager(Connection dbConn) {
        System.out.println("[ProfilManager] Initializing ProfilManager");
        this.dbConn = dbConn;
    }

    public String getName(int userid) throws SQLException {
        String sql = "SELECT Username FROM Users WHERE UserID = ?";
        try (PreparedStatement prep = this.dbConn.prepareStatement(sql)) {
            prep.setInt(1, userid);
            try (ResultSet rs = prep.executeQuery()) {
                return rs.next() ? rs.getString("Username") : "";
            }
        }
    }

    public void changeProfil(String password, int userId) throws SQLException {
        String sql = "UPDATE Users SET Password = ? WHERE UserID = ?";
        System.out.println("[DB] " + sql);
        String hashedPassword = PasswordUtil.hashPassword(password);
        try (PreparedStatement prep = this.dbConn.prepareStatement(sql)) {
            prep.setString(1, hashedPassword);
            prep.setInt(2, userId);
            prep.executeUpdate();
            System.out.println("[Account] Password updated");
        }
    }

    public boolean changeProfil(String password, String newUsername, String currentUsername, String icon, int userId) throws SQLException {
        if (!this.checkUsernameExist(newUsername, currentUsername)) {
            String normalizedIcon = extractIconName(icon);
            if (password != null && !password.isBlank()) {
                String sql = "UPDATE Users SET Password = ?, Username = ?, Icon = ? WHERE UserID = ?";
                System.out.println("[DB] " + sql);
                String hashedPassword = PasswordUtil.hashPassword(password);
                try (PreparedStatement prep = this.dbConn.prepareStatement(sql)) {
                    prep.setString(1, hashedPassword);
                    prep.setString(2, newUsername);
                    prep.setString(3, normalizedIcon);
                    prep.setInt(4, userId);
                    prep.executeUpdate();
                }
            } else {
                String sql = "UPDATE Users SET Username = ?, Icon = ? WHERE UserID = ?";
                System.out.println("[DB] " + sql);
                try (PreparedStatement prep = this.dbConn.prepareStatement(sql)) {
                    prep.setString(1, newUsername);
                    prep.setString(2, normalizedIcon);
                    prep.setInt(3, userId);
                    prep.executeUpdate();
                }
            }
            System.out.println("[Account] Updated profile for user ID " + userId);
            return true;
        }
        return false;
    }

    public boolean deleteUserById(int userId) {
        try {
            String sql = "DELETE FROM Users WHERE UserID = ?";
            try (PreparedStatement prep = this.dbConn.prepareStatement(sql)) {
                prep.setInt(1, userId);
                prep.executeUpdate();
            }
            System.out.println("[ProfilManager] Deleted user account with UserID " + userId);
            return true;
        } catch (SQLException e) {
            System.out.println("[ProfilManager] Failed to delete user account with UserID " + userId);
            e.printStackTrace();
            return false;
        }
    }

    public String getProfilOptionIconAsHTML(String profilIcon) {
        String iconDropDownAsHTML = "";
        iconDropDownAsHTML += "<option value='../img/anlaesse/Lumibella.webp' " + ("Lumibella".equals(profilIcon) ? "selected" : "") + ">Lumibella</option>";
        iconDropDownAsHTML += "<option value='../img/anlaesse/Novaterra.webp' " + ("Novaterra".equals(profilIcon) ? "selected" : "") + ">Novaterra</option>";
        iconDropDownAsHTML += "<option value='../img/anlaesse/Quixart.webp' " + ("Quixart".equals(profilIcon) ? "selected" : "") + ">Quixart</option>";
        return iconDropDownAsHTML;
    }

    public boolean checkUsernameExist(String newUsername, String currentUsername) throws NoConnectionException, SQLException {
        if (newUsername != null && newUsername.equals(currentUsername)) {
            return false;
        }
        String sql = "SELECT Username FROM Users WHERE Username = ?";
        System.out.println("[DB] " + sql);
        try (PreparedStatement prep = this.dbConn.prepareStatement(sql)) {
            prep.setString(1, newUsername);
            try (ResultSet rs = prep.executeQuery()) {
                return rs.next();
            }
        }
    }

    private String extractIconName(String icon) {
        if (icon == null || icon.isBlank()) {
            return "Lumibella";
        }
        int slash = icon.lastIndexOf('/');
        int dot = icon.lastIndexOf('.');
        if (slash >= 0 && dot > slash) {
            return icon.substring(slash + 1, dot);
        }
        if (dot > 0) {
            return icon.substring(0, dot);
        }
        return icon;
    }
}
