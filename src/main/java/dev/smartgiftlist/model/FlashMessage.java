package dev.smartgiftlist.model;

import java.io.Serial;
import java.io.Serializable;

/**
 * Immutable session flash banner carrying plain-text feedback across PRG redirects.
 */
public final class FlashMessage implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private final String title;
    private final String detail;
    private final boolean error;

    public FlashMessage(String title, String detail, boolean error) {
        this.title = title != null ? title : "";
        this.detail = detail != null ? detail : "";
        this.error = error;
    }

    public static FlashMessage info(String title, String detail) {
        return new FlashMessage(title, detail, false);
    }

    public static FlashMessage banner(String detail) {
        return new FlashMessage("", detail, false);
    }

    public static FlashMessage error(String title, String detail) {
        return new FlashMessage(title, detail, true);
    }

    public String getTitle() {
        return title;
    }

    public String getDetail() {
        return detail;
    }

    public boolean isError() {
        return error;
    }

    public boolean isHasTitle() {
        return !title.isEmpty();
    }
}
