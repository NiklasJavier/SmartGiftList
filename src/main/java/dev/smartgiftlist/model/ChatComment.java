package dev.smartgiftlist.model;

import java.time.LocalDateTime;

/**
 * Discussion message or system event posted to a wishlist thread.
 */
public final class ChatComment {

    private final int id;
    private final int wishlistId;
    private final Integer userId;
    private final String username;
    private final String userIcon;
    private final String text;
    private final CommentType type;
    private final LocalDateTime createdAt;

    public ChatComment(
            int id,
            int wishlistId,
            Integer userId,
            String username,
            String userIcon,
            String text,
            CommentType type,
            LocalDateTime createdAt
    ) {
        this.id = id;
        this.wishlistId = wishlistId;
        this.userId = userId;
        this.username = username != null ? username : "";
        this.userIcon = IconCatalog.normalizeAvatarKey(userIcon);
        this.text = text != null ? text : "";
        this.type = type != null ? type : CommentType.EVENT;
        this.createdAt = createdAt;
    }

    public int getId() {
        return id;
    }

    public int getWishlistId() {
        return wishlistId;
    }

    public Integer getUserId() {
        return userId;
    }

    public String getUsername() {
        return username;
    }

    public String getUserIcon() {
        return userIcon;
    }

    public String getAvatarPath() {
        return IconCatalog.avatarPathForKey(userIcon);
    }

    public String getText() {
        return text;
    }

    public CommentType getType() {
        return type;
    }

    public boolean isEvent() {
        return type == CommentType.EVENT;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
