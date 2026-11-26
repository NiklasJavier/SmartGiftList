package dev.smartgiftlist.model;

/**
 * Semantic display status of a gift item used for CSS row state styling.
 */
public enum GiftStatus {
    SINGLE("single"),
    GROUP("group"),
    SURPRISE("surprise"),
    CLAIMED("claimed");

    private final String key;

    GiftStatus(String key) {
        this.key = key;
    }

    public String getKey() {
        return key;
    }
}
