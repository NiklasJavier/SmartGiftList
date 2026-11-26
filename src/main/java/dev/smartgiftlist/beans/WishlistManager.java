package dev.smartgiftlist.beans;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Arrays;

import dev.smartgiftlist.security.HtmlUtil;

/**
 * Manages wishlist creation, lookup, sharing, participants, and card rendering.
 */
public class WishlistManager {

    private final Connection dbConn;

    public WishlistManager(Connection dbConn) {
        this.dbConn = dbConn;
    }

    public void createWishlist(int userID, String title, String event, Date eventDate, String description, String url, String icon) throws SQLException {
        String sql = "INSERT INTO Wishlists (UserID, NetID, Title, Event, Description, url, Icon, EventDate, CreateDate) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        System.out.println("[DB] " + sql);
        Timestamp timestamp = new Timestamp(System.currentTimeMillis());
        int lastThreeDigits = (int) (System.currentTimeMillis() % 1000);
        String generatedNetID = userID + "-" + countWishlists() + lastThreeDigits;

        System.out.println("[WishlistManager] Creating wishlist '" + title + "' (userID=" + userID + ", netID=" + generatedNetID + ")");
        try (PreparedStatement stmt = dbConn.prepareStatement(sql)) {
            stmt.setInt(1, userID);
            stmt.setString(2, generatedNetID);
            stmt.setString(3, title);
            stmt.setString(4, event);
            stmt.setString(5, description);
            stmt.setString(6, url);
            stmt.setString(7, icon);
            stmt.setDate(8, eventDate);
            stmt.setTimestamp(9, timestamp);
            stmt.executeUpdate();
            System.out.println("[WishlistManager] Created wishlist '" + title + "'");
        }
    }

    public String getWishlistCardsHtmlForUser(int userID) throws SQLException {
        StringBuilder htmlBuilder = new StringBuilder();
        String sql = "SELECT WishlistID, Title, Event, Description, Icon, NetID FROM Wishlists WHERE UserID = ?";
        System.out.println("[DB] " + sql);
        try (PreparedStatement stmt = dbConn.prepareStatement(sql)) {
            stmt.setInt(1, userID);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    appendWishlistCard(htmlBuilder, rs);
                }
            }
        }
        return htmlBuilder.toString();
    }

    public String getWishlistCardsHtmlForUserConnectedLists(int userID) throws SQLException {
        StringBuilder htmlBuilder = new StringBuilder();
        String sql = "SELECT w.WishlistID, w.Title, w.Event, w.Description, w.Icon, w.NetID "
                + "FROM Wishlists w "
                + "JOIN UserWishlistConnections uwc ON w.WishlistID = uwc.WishlistID "
                + "WHERE uwc.UserID = ?";
        System.out.println("[DB] " + sql);
        try (PreparedStatement stmt = dbConn.prepareStatement(sql)) {
            stmt.setInt(1, userID);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    appendWishlistCard(htmlBuilder, rs);
                }
            }
        }
        return htmlBuilder.toString();
    }

    private void appendWishlistCard(StringBuilder htmlBuilder, ResultSet rs) throws SQLException {
        String title = HtmlUtil.escape(rs.getString("Title"));
        String event = HtmlUtil.escape(rs.getString("Event"));
        String fullDescription = rs.getString("Description");
        String imageURL = HtmlUtil.escape(rs.getString("Icon"));
        String netID = HtmlUtil.escape(rs.getString("NetID"));
        String shortDescription = HtmlUtil.escape(shortenDescription(fullDescription, 25));

        htmlBuilder.append("<div class=\"owlc-card\">")
                .append("<div class=\"owlc-card-image\"><img src=\"").append(imageURL).append("\" alt=\"Wishlist cover\" /></div>")
                .append("<div class=\"owlc-card-content\">")
                .append("<h3>").append(title).append("</h3>")
                .append("<p>").append(event).append("</p>")
                .append("<form action=\"./OwnWishListAppl.jsp\" method=\"POST\" class=\"owlc-form\">")
                .append("<input type=\"hidden\" name=\"netId\" value=\"").append(netID).append("\" />")
                .append("<button onclick=\"saveScrollPosition()\" type=\"submit\" name=\"showWishlist\" value=\"showWishlist\" class=\"owlc-select-button\">Select</button>")
                .append("</form>")
                .append("<div class=\"description-separator\"></div>")
                .append("<p class=\"owlc-description\">").append(shortDescription).append("</p>")
                .append("</div>")
                .append("</div>");
    }

    public String getWishlistHTMLDropdownWithConnected(int userID, String currentNetID) throws SQLException {
        if (userID != 0) {
            StringBuilder dropdownBuilder = new StringBuilder("<select name=\"netId\" id=\"select-wishlist\">");
            String sqlOwn = "SELECT WishlistID, Title, netID FROM Wishlists WHERE UserID = ? ORDER BY WishlistID";
            String sqlConnected = "SELECT w.WishlistID, w.Title, w.netID FROM Wishlists w JOIN UserWishlistConnections uwc ON w.WishlistID = uwc.WishlistID WHERE uwc.UserID = ? AND uwc.IsOwner = FALSE ORDER BY w.WishlistID";

            try {
                try (PreparedStatement stmtOwn = dbConn.prepareStatement(sqlOwn)) {
                    stmtOwn.setInt(1, userID);
                    try (ResultSet rsOwn = stmtOwn.executeQuery()) {
                        while (rsOwn.next()) {
                            String netID = rsOwn.getString("netID");
                            String selectedAttribute = netID != null && netID.equals(currentNetID) ? " selected" : "";
                            dropdownBuilder.append(String.format("<option value=\"%s\"%s>%d - %s</option>",
                                    HtmlUtil.escape(netID), selectedAttribute, rsOwn.getInt("WishlistID"), HtmlUtil.escape(rsOwn.getString("Title"))));
                        }
                    }
                }

                try (PreparedStatement stmtConnected = dbConn.prepareStatement(sqlConnected)) {
                    stmtConnected.setInt(1, userID);
                    try (ResultSet rsConnected = stmtConnected.executeQuery()) {
                        while (rsConnected.next()) {
                            String netID = rsConnected.getString("netID");
                            String selectedAttribute = netID != null && netID.equals(currentNetID) ? " selected" : "";
                            dropdownBuilder.append(String.format("<option value=\"%s\"%s>%d - %s (shared)</option>",
                                    HtmlUtil.escape(netID), selectedAttribute, rsConnected.getInt("WishlistID"), HtmlUtil.escape(rsConnected.getString("Title"))));
                        }
                    }
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }

            dropdownBuilder.append("</select>");
            return dropdownBuilder.toString();
        }
        return "";
    }

    private String shortenDescription(String description, int wordLimit) {
        if (description == null || description.isBlank()) {
            return "";
        }
        String[] words = description.split("\\s+", wordLimit + 1);
        if (words.length > wordLimit) {
            return String.join(" ", Arrays.copyOf(words, wordLimit)) + "...";
        }
        return description;
    }

    public Wishlist getWishlistByNetID(String netID) throws SQLException {
        Wishlist wishlist = new Wishlist();
        String sql = "SELECT * FROM Wishlists WHERE netID = ?";
        System.out.println("[DB] " + sql);

        try (PreparedStatement stmt = dbConn.prepareStatement(sql)) {
            stmt.setString(1, netID);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    wishlist.setWishlistID(rs.getInt("wishlistID"));
                    wishlist.setNetid(rs.getString("netID"));
                    wishlist.setTitle(rs.getString("title"));
                    wishlist.setEvent(rs.getString("event"));
                    wishlist.setDescription(rs.getString("description"));
                    wishlist.setUrl(rs.getString("url"));
                    wishlist.setIcon(rs.getString("icon"));
                    wishlist.setEventDate(rs.getDate("eventDate"));
                    wishlist.setUserID(rs.getInt("userID"));
                }
            }
        }
        return wishlist;
    }

    public void deleteWishlist(String netID) throws SQLException {
        String sql = "DELETE FROM Wishlists WHERE NetID = ?";
        System.out.println("[DB] " + sql);
        try (PreparedStatement stmt = dbConn.prepareStatement(sql)) {
            stmt.setString(1, netID);
            stmt.executeUpdate();
            System.out.println("[WishlistManager] Deleted wishlist with NetID '" + netID + "'");
        }
    }

    public void updateWishlistDetails(String netid, String url, String event, String description, String icon, Date eventDate) throws SQLException {
        if (netid != null && !netid.isEmpty() && !"0".equals(netid)) {
            String sql = "UPDATE Wishlists SET url = ?, Event = ?, Description = ?, Icon = ?, EventDate = ? WHERE NetID = ?";
            System.out.println("[DB] " + sql);
            try (PreparedStatement stmt = dbConn.prepareStatement(sql)) {
                stmt.setString(1, url);
                stmt.setString(2, event);
                stmt.setString(3, description);
                stmt.setString(4, icon);
                stmt.setDate(5, eventDate);
                stmt.setString(6, netid);
                stmt.executeUpdate();
            }
        }
    }

    public String getIconDropdownHtml(String netId) throws SQLException {
        String selectedIcon = getSelectedIconForWishlist(netId);

        String[] icons = {"PaperList", "BrozeList", "SilverList", "GoldenList"};
        String[] iconPaths = {
            "../img/anlaesse/PaperList.webp",
            "../img/anlaesse/BrozeList.webp",
            "../img/anlaesse/SilverList.webp",
            "../img/anlaesse/GoldenList.webp"
        };

        StringBuilder htmlBuilder = new StringBuilder();
        for (int i = 0; i < icons.length; i++) {
            String selectedAttribute = iconPaths[i].equals(selectedIcon) ? " selected" : "";
            htmlBuilder.append(String.format("<option value=\"%s\"%s>%s</option>",
                    iconPaths[i], selectedAttribute, icons[i]));
        }
        return htmlBuilder.toString();
    }

    private String getSelectedIconForWishlist(String netId) throws SQLException {
        String sql = "SELECT Icon FROM Wishlists WHERE NetID = ?";
        try (PreparedStatement stmt = dbConn.prepareStatement(sql)) {
            stmt.setString(1, netId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("Icon");
                }
            }
        }
        return "";
    }

    public int countWishlists() throws SQLException {
        String sql = "SELECT COUNT(*) AS total FROM Wishlists";
        System.out.println("[DB] " + sql);
        try (PreparedStatement stmt = dbConn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            if (rs.next()) {
                return rs.getInt("total");
            }
        }
        return 0;
    }

    public String getWishlistParticipantsHTML(int wishlistID) throws SQLException {
        if (wishlistID != 0) {
            StringBuilder htmlBuilder = new StringBuilder();
            String query = "SELECT u.UserID, u.Username, u.Icon "
                    + "FROM UserWishlistConnections uwc "
                    + "JOIN Users u ON uwc.UserID = u.UserID "
                    + "WHERE uwc.WishlistID = ?";

            try (PreparedStatement pstmt = dbConn.prepareStatement(query)) {
                pstmt.setInt(1, wishlistID);
                try (ResultSet rs = pstmt.executeQuery()) {
                    while (rs.next()) {
                        String iconName = rs.getString("Icon");
                        String iconPath = "../img/anlaesse/" + HtmlUtil.escape(iconName != null ? iconName : "Lumibella") + ".webp";
                        int userID = rs.getInt("UserID");
                        String username = HtmlUtil.escape(rs.getString("Username"));

                        htmlBuilder.append("<tr>")
                                .append("<td><img src=\"").append(iconPath).append("\" alt=\"Profile picture\" class=\"owlu-participant-image\"></td>")
                                .append("<td>").append(userID).append("</td>")
                                .append("<td>").append(username).append("</td>")
                                .append("</tr>");
                    }
                }
            }
            return htmlBuilder.toString();
        }
        return "";
    }

    public boolean isWishlistOwnedByUser(int wishlistID, int userID) {
        if (wishlistID != 0 && userID != 0) {
            String sql = "SELECT COUNT(*) FROM Wishlists WHERE WishlistID = ? AND UserID = ?";
            try (PreparedStatement stmt = dbConn.prepareStatement(sql)) {
                stmt.setInt(1, wishlistID);
                stmt.setInt(2, userID);
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        return rs.getInt(1) > 0;
                    }
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    public boolean assignUserToWishlist(int userID, int wishlistID) throws SQLException {
        if (userID != 0 && wishlistID != 0 && !this.isWishlistOwnedByUser(wishlistID, userID)) {
            String checkSql2 = "SELECT WishlistID, UserID FROM UserWishlistConnections WHERE WishlistID = ? AND UserID = ?";
            try (PreparedStatement checkStmt = dbConn.prepareStatement(checkSql2)) {
                checkStmt.setInt(1, wishlistID);
                checkStmt.setInt(2, userID);
                try (ResultSet rs = checkStmt.executeQuery()) {
                    if (rs.next()) {
                        System.out.println("[WishlistManager] User " + userID + " is already connected to wishlist " + wishlistID);
                        return false;
                    }
                }
            }

            String sql = "INSERT INTO UserWishlistConnections (UserID, WishlistID, IsOwner, LinkDate) VALUES (?, ?, false, CURRENT_TIMESTAMP)";
            try (PreparedStatement stmt = dbConn.prepareStatement(sql)) {
                stmt.setInt(1, userID);
                stmt.setInt(2, wishlistID);
                stmt.executeUpdate();
                System.out.println("[WishlistManager] Connected user " + userID + " to wishlist " + wishlistID);
                return true;
            }
        }
        return false;
    }

    public void removeUserFromWishlist(int userID, int wishlistID) throws SQLException {
        if (userID != 0 && wishlistID != 0) {
            String sql = "DELETE FROM UserWishlistConnections WHERE UserID = ? AND WishlistID = ?";
            try (PreparedStatement stmt = dbConn.prepareStatement(sql)) {
                stmt.setInt(1, userID);
                stmt.setInt(2, wishlistID);
                stmt.executeUpdate();
                System.out.println("[WishlistManager] Removed user " + userID + " from wishlist " + wishlistID);
            }
        }
    }
}
