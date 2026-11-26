package dev.smartgiftlist.model;

/**
 * Discriminator for wishlist discussion messages stored in WishlistComments.
 */
public enum CommentType {
    USER(1),
    EVENT(2);

    private final int code;

    CommentType(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }

    public static CommentType fromCode(int code) {
        return code == 1 ? USER : EVENT;
    }
}
