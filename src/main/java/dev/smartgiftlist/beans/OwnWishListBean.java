package dev.smartgiftlist.beans;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import dev.smartgiftlist.jdbc.NoConnectionException;
import dev.smartgiftlist.jdbc.PostgreSQLAccess;

/**
 * Primary session facade coordinating wishlists, gifts, chat comments, and user profile state.
 */
public class OwnWishListBean {

    private final Connection dbConn;
    private int userId;
    private String netid;
    private int wishlistID;
    private int giftID;
    private String profilUsername;
    private String profilIcon;

    private final WishlistManager wishlistManager;
    private final GiftManager giftManager;
    private final ChatManager chatManager;
    private final ProfilManager profilManager;
    private Wishlist wishlist;
    private Gift gift;

    public OwnWishListBean() throws NoConnectionException {
        System.out.println("[OwnWishListBean] Initializing OwnWishListBean");
        this.dbConn = new PostgreSQLAccess().getConnection();
        this.wishlistManager = new WishlistManager(this.dbConn);
        this.giftManager = new GiftManager(this.dbConn);
        this.chatManager = new ChatManager(this.dbConn);
        this.profilManager = new ProfilManager(this.dbConn);
        this.userId = 0;
        this.netid = "";
        this.wishlistID = 0;
        this.wishlist = new Wishlist();
        this.gift = new Gift();
        this.giftID = 0;
        this.profilIcon = "";
        this.profilUsername = "";
    }

    public void setSessionLogoutParameter() {
        System.out.println("[Session] User signed out; resetting session parameters");
        this.userId = 0;
        this.netid = "";
        this.wishlistID = 0;
        this.wishlist = new Wishlist();
        this.gift = new Gift();
        this.giftID = 0;
        this.profilIcon = "";
        this.profilUsername = "";
    }

    public void fetchAndSetUserIdByUsername(String username) throws SQLException {
        String sql = "SELECT UserID FROM Users WHERE Username = ?";
        System.out.println("[DB] " + sql);
        try (PreparedStatement stmt = dbConn.prepareStatement(sql)) {
            stmt.setString(1, username);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    this.setUserId(rs.getInt("UserID"));
                    System.out.println("[DB] Selected user '" + username + "' (UserID=" + this.userId + ")");
                    if (this.wishlist.getWishlistID() == 0 && this.userId != 0 && (this.netid == null || this.netid.isEmpty())) {
                        this.setProfilUsername(username);
                        fetchAndSetLastNetIDForUser();
                        getWishlistByNetID();
                    }
                } else {
                    throw new SQLException("[DB] User not found: " + username);
                }
            }
        }
    }

    public void fetchAndSetLastNetIDForUser() throws SQLException {
        if (this.userId != 0) {
            String sql = "SELECT wishlistID, netID FROM Wishlists WHERE UserID = ? ORDER BY CreateDate DESC LIMIT 1";
            System.out.println("[DB] " + sql);
            try (PreparedStatement stmt = dbConn.prepareStatement(sql)) {
                stmt.setInt(1, this.userId);
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        System.out.println("[Wishlist] Loaded initial wishlist NetID=" + rs.getString("netID") + ", WishlistID=" + rs.getInt("wishlistID"));
                        this.setWishlistID(rs.getInt("wishlistID"));
                        this.setNetID(rs.getString("netID"));
                    } else {
                        this.setWishlistID(0);
                        this.setNetID("");
                    }
                }
            }
        }
    }

    public void fetchWishListIDForUserByNetID() throws SQLException {
        if (this.netid != null && !this.netid.isEmpty()) {
            String sql = "SELECT WishlistID, netID FROM Wishlists WHERE netID = ?";
            System.out.println("[DB] " + sql);
            try (PreparedStatement stmt = dbConn.prepareStatement(sql)) {
                stmt.setString(1, this.netid);
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        System.out.println("[Wishlist] Selected wishlist NetID=" + rs.getString("netID") + ", WishlistID=" + rs.getInt("wishlistID"));
                        this.setWishlistID(rs.getInt("WishlistID"));
                    } else {
                        System.out.println("[Wishlist] No wishlist found for NetID=" + this.netid);
                        this.setWishlistID(0);
                    }
                }
            } catch (SQLException e) {
                System.out.println("[Wishlist] Failed to resolve WishlistID from NetID");
                e.printStackTrace();
            }
        }
    }

    public void fetchAccountDataForView() throws SQLException {
        String sql = "SELECT Icon FROM Users WHERE UserID = ?";
        System.out.println("[DB] " + sql);
        try (PreparedStatement stmt = dbConn.prepareStatement(sql)) {
            stmt.setInt(1, this.userId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    this.setProfilIcon(rs.getString("Icon"));
                }
            }
        }
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getNetID() {
        return netid;
    }

    public void setNetID(String netid) {
        System.out.println("[OwnWishListBean] Setting NetID=" + netid);
        this.netid = netid != null ? netid : "";
        try {
            fetchWishListIDForUserByNetID();
        } catch (SQLException e) {
            System.out.println("[Wishlist] Failed to resolve WishlistID for NetID=" + netid);
            e.printStackTrace();
        }
    }

    public int getWishlistID() {
        return wishlistID;
    }

    public void setWishlistID(int wishlistID) {
        this.wishlistID = wishlistID;
    }

    public int getGiftID() {
        return giftID;
    }

    public void setGiftID(int giftID) {
        this.giftID = giftID;
    }

    public ProfilManager getProfilManager() {
        return profilManager;
    }

    public Wishlist getWishlist() {
        return wishlist;
    }

    public Gift getGift() {
        return gift;
    }

    public void setGift(Gift gift) {
        this.gift = gift;
    }

    public String getProfilUsername() {
        return profilUsername;
    }

    public void setProfilUsername(String profilUsername) {
        this.profilUsername = profilUsername;
    }

    public String getProfilIcon() {
        return this.profilIcon;
    }

    public void setProfilIcon(String profilIcon) {
        this.profilIcon = profilIcon;
    }

    public void setWishlist(Wishlist wishlist) {
        this.wishlist = wishlist;
    }

    /* ----- Wishlist Management ----- */

    public void createWishlist(String title, String event, Date eventDate, String description, String url, String icon) throws SQLException {
        wishlistManager.createWishlist(this.getUserId(), title, event, eventDate, description, url, icon);
    }

    public String getWishlistCardsHtmlForUser() throws SQLException {
        return wishlistManager.getWishlistCardsHtmlForUser(this.userId);
    }

    public String getWishlistCardsHtmlForUserConnectedLists() throws SQLException {
        return wishlistManager.getWishlistCardsHtmlForUserConnectedLists(this.userId);
    }

    public String getWishlistHTMLDropdownWithConnected() throws SQLException {
        return wishlistManager.getWishlistHTMLDropdownWithConnected(this.userId, this.netid);
    }

    public void getWishlistByNetID() throws SQLException {
        if (this.netid != null && !this.netid.isEmpty()) {
            this.wishlist = wishlistManager.getWishlistByNetID(this.netid);
        } else {
            this.wishlist = new Wishlist();
        }
    }

    public void deleteWishlist() throws SQLException {
        if (wishlistManager.isWishlistOwnedByUser(this.wishlistID, this.userId)) {
            if (this.netid != null && !this.netid.isEmpty()) {
                wishlistManager.deleteWishlist(this.getNetID());
            }
        } else {
            wishlistManager.removeUserFromWishlist(this.userId, this.wishlistID);
        }
    }

    public void updateWishlistDetails(String url, String event, String description, String icon, Date eventDate) throws SQLException {
        if (wishlistManager.isWishlistOwnedByUser(this.wishlistID, this.userId)) {
            wishlistManager.updateWishlistDetails(this.getNetID(), url, event, description, icon, eventDate);
        }
    }

    public String getIconDropdownHtml() throws SQLException {
        return wishlistManager.getIconDropdownHtml(this.netid);
    }

    public String getWishlistParticipantsHTML() throws SQLException {
        return wishlistManager.getWishlistParticipantsHTML(this.wishlistID);
    }

    public boolean assignUserToWishlist() throws SQLException {
        return wishlistManager.assignUserToWishlist(this.userId, this.wishlistID);
    }

    public void removeUserFromWishlist() throws SQLException {
        wishlistManager.removeUserFromWishlist(this.userId, this.wishlistID);
    }

    /* ----- Gift Management ----- */

    public void addGift(String title, String description, String url, String icon, double price, int numberOfPeople, boolean surprise) throws SQLException {
        giftManager.addGift(this.wishlistID, title, description, url, icon, price, numberOfPeople, surprise, this.userId);
    }

    public String getGiftListForWishlistAsHtml() {
        return giftManager.getGiftListForWishlistAsHtml(this.wishlistID);
    }

    public void deleteGift() throws SQLException {
        giftManager.deleteGift(this.giftID);
    }

    public void getGiftByGiftID() throws SQLException {
        if (this.netid != null && !this.netid.isEmpty()) {
            this.gift = giftManager.getGiftByGiftID(this.giftID);
        } else {
            this.gift = new Gift();
        }
    }

    public boolean assignUserToGift() throws SQLException {
        return giftManager.assignUserToGift(this.userId, this.giftID);
    }

    public void removeUserFromGift() throws SQLException {
        giftManager.removeUserFromGift(this.userId, this.giftID);
    }

    public String getUserGiftConnectionsHTML() throws SQLException {
        return giftManager.getUserGiftConnectionsHTML(this.giftID);
    }

    public void deleteGift(int giftID) throws SQLException {
        giftManager.deleteGift(giftID);
    }

    public boolean assignUserToGift(int giftID) throws SQLException {
        return giftManager.assignUserToGift(this.userId, giftID);
    }

    public void removeUserFromGift(int giftID) throws SQLException {
        giftManager.removeUserFromGift(this.userId, giftID);
    }

    /* ----- Chat Comment Management ----- */

    public String getRenderWishlistComments() throws SQLException {
        return chatManager.getRenderWishlistComments(this.wishlistID);
    }

    public void addNormalMessage(String commentText) throws SQLException {
        chatManager.addNormalMessage(this.wishlistID, this.userId, commentText);
    }

    public void withdrawLastUserMessage() throws SQLException {
        chatManager.withdrawLastUserMessage(this.userId, this.wishlistID);
    }

    /* ----- Profile Management ----- */

    public boolean deleteUserById() throws SQLException {
        return profilManager.deleteUserById(this.userId);
    }

    public boolean changeProfil(String password, String newUsername, String icon) throws SQLException {
        return profilManager.changeProfil(password, newUsername, this.profilUsername, icon, this.userId);
    }

    public String getProfilOptionIconAsHTML() {
        return this.profilManager.getProfilOptionIconAsHTML(this.profilIcon);
    }
}
