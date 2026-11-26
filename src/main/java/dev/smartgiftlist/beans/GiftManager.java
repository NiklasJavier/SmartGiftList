package dev.smartgiftlist.beans;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import dev.smartgiftlist.security.HtmlUtil;

/**
 * Manages CRUD operations, claims, and HTML rendering for gifts.
 */
public class GiftManager {

    private final Connection dbConn;

    public GiftManager(Connection dbConn) {
        this.dbConn = dbConn;
    }

    public void addGift(int wishlistID, String title, String description, String url, String icon, double price, int numberOfPeople, boolean surprise, int creatorUserID) throws SQLException {
        if (wishlistID != 0 && creatorUserID != 0) {
            System.out.println("[GiftManager] Adding gift '" + title + "' to wishlist " + wishlistID + " (people=" + numberOfPeople + ", surprise=" + surprise + ")");
            String sql = "INSERT INTO Gifts (WishlistID, Title, Description, Url, Icon, Price, NumberOfPeople, Surprise, CreatorUserID, CreateDate) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, NOW())";
            try (PreparedStatement stmt = dbConn.prepareStatement(sql)) {
                stmt.setInt(1, wishlistID);
                stmt.setString(2, title);
                stmt.setString(3, description);
                stmt.setString(4, url);
                stmt.setString(5, icon);
                stmt.setDouble(6, price);
                stmt.setInt(7, numberOfPeople);
                stmt.setBoolean(8, surprise);
                stmt.setInt(9, creatorUserID);
                stmt.executeUpdate();
                System.out.println("[GiftManager] Saved gift '" + title + "' to wishlist " + wishlistID);
            }
        }
    }

    public String getGiftListForWishlistAsHtml(int wishlistId) {
        if (wishlistId == 0) {
            return "";
        }

        StringBuilder htmlBuilder = new StringBuilder();
        String query = "SELECT w.WishlistID, g.GiftID, g.Title, g.NumberOfPeople, g.Surprise, g.Description, "
                + "COUNT(ugc.UserID) AS UserConnections, STRING_AGG(DISTINCT u.Icon, ',') AS Icons "
                + "FROM Gifts g "
                + "LEFT JOIN UserGiftConnections ugc ON g.GiftID = ugc.GiftID "
                + "JOIN Wishlists w ON g.WishlistID = w.WishlistID "
                + "LEFT JOIN Users u ON ugc.UserID = u.UserID "
                + "WHERE w.WishlistID = ? "
                + "GROUP BY g.GiftID, w.WishlistID, g.Title, g.NumberOfPeople, g.Surprise, g.Description "
                + "ORDER BY g.GiftID";

        try (PreparedStatement pstmt = dbConn.prepareStatement(query)) {
            pstmt.setInt(1, wishlistId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    String dataStatus = determineDataStatus(rs.getInt("NumberOfPeople"), rs.getInt("UserConnections"), rs.getBoolean("Surprise"));
                    String giftType = determineGiftType(rs.getInt("NumberOfPeople"), rs.getInt("UserConnections"), rs.getBoolean("Surprise"));
                    String truncatedTitle = HtmlUtil.escape(truncateText(rs.getString("Title"), 12));
                    String truncatedDescription = HtmlUtil.escape(truncateText(rs.getString("Description"), 33));

                    htmlBuilder.append("<tr data-status=\"").append(dataStatus).append("\">")
                            .append("<td><input type=\"checkbox\" name=\"owlpGiftIDForSelect\" value='").append(rs.getInt("GiftID")).append("' class=\"owlp-select-checkbox\"></td>")
                            .append("<td>").append(truncatedTitle).append("</td>")
                            .append("<td>").append(truncatedDescription).append("</td>")
                            .append("<td>").append(giftType).append("</td>")
                            .append("<td><div class=\"owlp-profile-images\">");

                    String rawIcons = rs.getString("Icons");
                    if (rawIcons != null && !rawIcons.isEmpty()) {
                        String[] icons = rawIcons.split(",");
                        for (String iconName : icons) {
                            if (!iconName.isBlank()) {
                                String iconPath = "../img/anlaesse/" + HtmlUtil.escape(iconName.trim()) + ".webp";
                                htmlBuilder.append("<img src=\"").append(iconPath).append("\" alt=\"Profile picture\" class=\"owlp-profile-img\">");
                            }
                        }
                    }
                    htmlBuilder.append("</div></td></tr>");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("[GiftManager] Refreshed gift list for wishlist " + wishlistId);
        return htmlBuilder.toString();
    }

    private String determineDataStatus(int numberOfPeople, int userConnections, boolean surprise) {
        if (userConnections >= numberOfPeople && numberOfPeople > 0) {
            return "claimed";
        } else if (surprise) {
            return "surprise";
        } else if (numberOfPeople == 1) {
            return "single";
        } else if (numberOfPeople > 1) {
            return "group";
        }
        return "";
    }

    private String determineGiftType(int numberOfPeople, int userConnections, boolean surprise) {
        StringBuilder giftType = new StringBuilder();
        if (surprise) {
            giftType.append("SG"); // Surprise Gift
        }
        if (numberOfPeople == 1 && !surprise) {
            giftType.append(giftType.length() > 0 ? ", IG" : "IG"); // Individual Gift
        } else if (numberOfPeople > 1 && !surprise) {
            giftType.append(giftType.length() > 0 ? ", GG" : "GG"); // Group Gift
        }
        if (userConnections >= numberOfPeople && numberOfPeople > 0) {
            giftType.append(giftType.length() > 0 ? ", CL" : "CL"); // Claimed
        }
        return giftType.toString();
    }

    private String truncateText(String text, int maxLength) {
        if (text != null && text.length() > maxLength) {
            return text.substring(0, maxLength) + "...";
        }
        return text != null ? text : "";
    }

    public void deleteGift(int giftID) throws SQLException {
        if (giftID != 0) {
            String sql = "DELETE FROM Gifts WHERE GiftID = ?";
            try (PreparedStatement stmt = dbConn.prepareStatement(sql)) {
                stmt.setInt(1, giftID);
                stmt.executeUpdate();
                System.out.println("[GiftManager] Deleted gift with ID " + giftID);
            }
        }
    }

    public Gift getGiftByGiftID(int giftID) throws SQLException {
        Gift gift = new Gift();
        if (giftID != 0) {
            String sql = "SELECT * FROM Gifts WHERE GiftID = ?";
            try (PreparedStatement stmt = dbConn.prepareStatement(sql)) {
                stmt.setInt(1, giftID);
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        int wishlistID = rs.getInt("WishlistID");
                        String title = rs.getString("Title");
                        String description = rs.getString("Description");
                        String url = rs.getString("Url");
                        String icon = rs.getString("Icon");
                        double price = rs.getDouble("Price");
                        int numberOfPeople = rs.getInt("NumberOfPeople");
                        Boolean isSurprise = rs.getBoolean("Surprise");

                        gift = new Gift(giftID, wishlistID, title, description, url, icon, price, numberOfPeople, isSurprise);
                        System.out.println("[GiftManager] Loaded gift '" + title + "' (ID " + giftID + ")");
                    }
                }
            }
        }
        return gift;
    }

    public boolean assignUserToGift(int userID, int giftID) throws SQLException {
        if (userID != 0 && giftID != 0) {
            String checkSql = "SELECT COUNT(*) AS count, g.NumberOfPeople FROM UserGiftConnections ugc JOIN Gifts g ON ugc.GiftID = g.GiftID WHERE ugc.GiftID = ? GROUP BY g.NumberOfPeople";
            try (PreparedStatement checkStmt = dbConn.prepareStatement(checkSql)) {
                checkStmt.setInt(1, giftID);
                try (ResultSet rs = checkStmt.executeQuery()) {
                    if (rs.next()) {
                        int currentCount = rs.getInt("count");
                        int numberOfPeople = rs.getInt("NumberOfPeople");
                        if (currentCount >= numberOfPeople) {
                            System.out.println("[GiftManager] Gift " + giftID + " has already reached the maximum number of participants.");
                            return false;
                        }
                    }
                }
            }

            String checkSql2 = "SELECT GiftID, UserID FROM UserGiftConnections WHERE GiftID = ? AND UserID = ?";
            try (PreparedStatement checkStmt = dbConn.prepareStatement(checkSql2)) {
                checkStmt.setInt(1, giftID);
                checkStmt.setInt(2, userID);
                try (ResultSet rs = checkStmt.executeQuery()) {
                    if (rs.next()) {
                        System.out.println("[GiftManager] User " + userID + " has already claimed gift " + giftID);
                        return false;
                    }
                }
            }

            String sql = "INSERT INTO UserGiftConnections (UserID, GiftID, LinkDate) VALUES (?, ?, CURRENT_TIMESTAMP)";
            try (PreparedStatement stmt = dbConn.prepareStatement(sql)) {
                stmt.setInt(1, userID);
                stmt.setInt(2, giftID);
                stmt.executeUpdate();
                System.out.println("[GiftManager] Assigned user " + userID + " to gift " + giftID);
                return true;
            }
        }
        return false;
    }

    public void removeUserFromGift(int userID, int giftID) throws SQLException {
        if (userID != 0 && giftID != 0) {
            String sql = "DELETE FROM UserGiftConnections WHERE UserID = ? AND GiftID = ?";
            try (PreparedStatement stmt = dbConn.prepareStatement(sql)) {
                stmt.setInt(1, userID);
                stmt.setInt(2, giftID);
                stmt.executeUpdate();
                System.out.println("[GiftManager] Removed user " + userID + " from gift " + giftID);
            }
        }
    }

    public String getUserGiftConnectionsHTML(int giftID) throws SQLException {
        if (giftID != 0) {
            StringBuilder htmlBuilder = new StringBuilder();
            String sql = "SELECT u.Icon, u.UserID, u.Username FROM UserGiftConnections ugc "
                    + "JOIN Users u ON ugc.UserID = u.UserID "
                    + "WHERE ugc.GiftID = ?";

            try (PreparedStatement stmt = dbConn.prepareStatement(sql)) {
                stmt.setInt(1, giftID);
                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        String iconName = rs.getString("Icon");
                        String username = HtmlUtil.escape(rs.getString("Username"));
                        String iconPath = String.format("../img/anlaesse/%s.webp",
                                HtmlUtil.escape(iconName != null ? iconName : "Lumibella"));

                        htmlBuilder.append(String.format(
                                "<li class=\"Owlpde-taken-item\">"
                                + "<img src=\"%s\" alt=\"Profile picture\" class=\"Owlpde-profile-pic\">"
                                + "<span class=\"Owlpde-user-info\">%s</span>"
                                + "</li>",
                                iconPath, username));
                    }
                }
            } catch (SQLException e) {
                e.printStackTrace();
                return "";
            }
            return htmlBuilder.toString();
        }
        return "";
    }
}
