package dev.smartgiftlist.model;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Immutable user account domain model.
 */
public final class User {

    private final int id;
    private final String username;
    private final String email;
    private final String passwordHash;
    private final LocalDate birthday;
    private final boolean admin;
    private final String icon;
    private final LocalDateTime joinDate;

    public User(
            int id,
            String username,
            String email,
            String passwordHash,
            LocalDate birthday,
            boolean admin,
            String icon,
            LocalDateTime joinDate
    ) {
        this.id = id;
        this.username = username != null ? username : "";
        this.email = email != null ? email : "";
        this.passwordHash = passwordHash != null ? passwordHash : "";
        this.birthday = birthday;
        this.admin = admin;
        this.icon = IconCatalog.normalizeAvatarKey(icon);
        this.joinDate = joinDate;
    }

    public int getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public String getEmail() {
        return email;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public LocalDate getBirthday() {
        return birthday;
    }

    public String getBirthdayIso() {
        return birthday != null ? birthday.toString() : "";
    }

    public boolean isAdmin() {
        return admin;
    }

    public String getIcon() {
        return icon;
    }

    public String getAvatarPath() {
        return IconCatalog.avatarPathForKey(icon);
    }

    public LocalDateTime getJoinDate() {
        return joinDate;
    }
}
