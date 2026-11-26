package dev.smartgiftlist.beans;

/**
 * Represents a single discussion or system message on a wishlist.
 */
public class ChatMessage {
    private String type; // "normal" or "event"
    private String userIcon; // Path to user avatar icon
    private String userName; // Author username
    private String text; // Message body

    public ChatMessage(String type, String userIcon, String userName, String text) {
        this.type = type;
        this.userIcon = userIcon;
        this.userName = userName;
        this.text = text;
    }

    public String getType() {
        return type;
    }

    public String getUserIcon() {
        return userIcon;
    }

    public String getUserName() {
        return userName;
    }

    public String getText() {
        return text;
    }
}
