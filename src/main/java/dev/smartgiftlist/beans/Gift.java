package dev.smartgiftlist.beans;

/**
 * Domain model representing a gift item on a wishlist.
 */
public class Gift {
    private int giftID;
    private String title;
    private String description;
    private String url;
    private String icon;
    private double price;
    private int numberOfPeople;
    private int wishlistID;
    private boolean isSuprise;

    public Gift(int giftID, int wishlistID, String title, String description, String url, String icon, double price, int numberOfPeople, Boolean isSuprise) {
        this.giftID = giftID;
        this.title = title;
        this.description = description;
        this.url = url;
        this.icon = icon;
        this.price = price;
        this.numberOfPeople = numberOfPeople;
        this.wishlistID = wishlistID;
        this.isSuprise = isSuprise != null && isSuprise;
    }

    public Gift() {
        this.giftID = 0;
        this.title = "";
        this.description = "";
        this.url = "";
        this.icon = "../img/anlaesse/PaperList.webp";
        this.price = 0.00;
        this.numberOfPeople = 0;
        this.wishlistID = 0;
        this.isSuprise = false;
    }

    public String getGiftID() {
        if (giftID == 0) {
            return "";
        }
        return String.valueOf(giftID);
    }

    public void setGiftID(int giftID) {
        this.giftID = giftID;
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

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public boolean isSuprise() {
        return isSuprise;
    }

    public void setSuprise(boolean isSuprise) {
        this.isSuprise = isSuprise;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public String getPrice() {
        if (price == 0.00) {
            return "";
        }
        return String.valueOf(price);
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public String getNumberOfPeople() {
        if (numberOfPeople == 0) {
            return "";
        }
        return String.valueOf(numberOfPeople);
    }

    public void setNumberOfPeople(int numberOfPeople) {
        this.numberOfPeople = numberOfPeople;
    }

    public int getWishlistID() {
        return wishlistID;
    }

    public void setWishlistID(int wishlistID) {
        this.wishlistID = wishlistID;
    }

    public String getWishlistType() {
        if (numberOfPeople == 0) {
            return "";
        }
        if (numberOfPeople == 1) {
            return isSuprise ? "Surprise Gift, Individual Gift" : "Individual Gift";
        }
        return isSuprise ? "Surprise Gift, Group Gift" : "Group Gift";
    }
}
