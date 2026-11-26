package dev.smartgiftlist.model;

/**
 * User participating in or owning a wishlist.
 */
public final class WishlistParticipant {

    private final int userId;
    private final String username;
    private final String icon;
    private final boolean owner;

    public WishlistParticipant(int userId, String username, String icon, boolean owner) {
        this.userId = userId;
        this.username = username != null ? username : "";
        this.icon = IconCatalog.normalizeAvatarKey(icon);
        this.owner = owner;
    }

    public int getUserId() {
        return userId;
    }

    public String getUsername() {
        return username;
    }

    public String getIcon() {
        return icon;
    }

    public boolean isOwner() {
        return owner;
    }

    public String getAvatarPath() {
        return IconCatalog.avatarPathForKey(icon);
    }
}
