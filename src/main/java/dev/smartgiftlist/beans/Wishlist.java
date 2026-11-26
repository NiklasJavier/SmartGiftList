package dev.smartgiftlist.beans;

import java.sql.Date;

/**
 * Domain model representing a gift wishlist.
 */
public class Wishlist {
    private int wishlistID;
    private String netid;
    private String title;
    private String event;
    private String description;
    private String url;
    private String icon;
    private Date eventDate;
    private int userID; // Owner user ID
    private Date createDate;

    public Wishlist(int wishlistID, String netid, String title, String event, String description, String url, String icon, Date eventDate, int userID) {
        super();
        this.wishlistID = wishlistID;
        this.netid = netid;
        this.title = title;
        this.event = event;
        this.description = description;
        this.url = url;
        this.icon = icon;
        this.eventDate = eventDate;
        this.userID = userID;
    }

    public Wishlist() {
        this.wishlistID = 0;
        this.netid = "";
        this.title = "";
        this.event = "";
        this.description = "";
        this.url = "";
        this.icon = "";
        this.eventDate = null;
        this.userID = 0;
    }

    public int getWishlistID() {
        return wishlistID;
    }

    public void setWishlistID(int wishlistID) {
        this.wishlistID = wishlistID;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public Date getEventDate() {
        return eventDate;
    }

    public void setEventDate(Date eventDate) {
        this.eventDate = eventDate;
    }

    public int getUserID() {
        return userID;
    }

    public void setUserID(int userID) {
        this.userID = userID;
    }

    public String getNetid() {
        return netid;
    }

    public void setNetid(String netid) {
        this.netid = netid;
    }

    public String getEvent() {
        return event;
    }

    public void setEvent(String event) {
        this.event = event;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public Date getCreateDate() {
        return createDate;
    }

    public void setCreateDate(Date createDate) {
        this.createDate = createDate;
    }
}
