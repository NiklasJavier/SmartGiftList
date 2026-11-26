package dev.smartgiftlist.model;

import java.time.LocalDate;
import java.time.LocalDateTime;

import dev.smartgiftlist.security.HtmlUtil;

/**
 * Immutable wishlist domain model.
 */
public final class Wishlist {

    private final int id;
    private final String netId;
    private final String title;
    private final String event;
    private final LocalDate eventDate;
    private final String description;
    private final String url;
    private final String icon;
    private final int ownerUserId;
    private final LocalDateTime createDate;

    public Wishlist(
            int id,
            String netId,
            String title,
            String event,
            LocalDate eventDate,
            String description,
            String url,
            String icon,
            int ownerUserId,
            LocalDateTime createDate
    ) {
        this.id = id;
        this.netId = netId != null ? netId : "";
        this.title = title != null ? title : "";
        this.event = event != null ? event : "";
        this.eventDate = eventDate;
        this.description = description != null ? description : "";
        this.url = url != null ? url : "";
        this.icon = IconCatalog.normalizeCoverPath(icon);
        this.ownerUserId = ownerUserId;
        this.createDate = createDate;
    }

    public int getId() {
        return id;
    }

    public String getNetId() {
        return netId;
    }

    public String getTitle() {
        return title;
    }

    public String getEvent() {
        return event;
    }

    public LocalDate getEventDate() {
        return eventDate;
    }

    public String getEventDateIso() {
        return eventDate != null ? eventDate.toString() : "";
    }

    public String getDescription() {
        return description;
    }

    public String getShortDescription() {
        return HtmlUtil.truncateWords(description, 25);
    }

    public String getUrl() {
        return url;
    }

    public String getIcon() {
        return icon;
    }

    public int getOwnerUserId() {
        return ownerUserId;
    }

    public LocalDateTime getCreateDate() {
        return createDate;
    }
}
