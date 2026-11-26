package dev.smartgiftlist.model;

/**
 * Participant who claimed a share in a gift item.
 */
public final class GiftClaimer {

    private final int userId;
    private final String username;
    private final String icon;

    public GiftClaimer(int userId, String username, String icon) {
        this.userId = userId;
        this.username = username != null ? username : "";
        this.icon = IconCatalog.normalizeAvatarKey(icon);
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

    public String getAvatarPath() {
        return IconCatalog.avatarPathForKey(icon);
    }
}
