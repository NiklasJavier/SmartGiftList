package dev.smartgiftlist.service;

import dev.smartgiftlist.model.FlashMessage;

/**
 * Typed service operation outcome carrying optional value and user flash feedback.
 */
public final class ServiceResult<T> {

    private final boolean ok;
    private final T value;
    private final FlashMessage flash;

    private ServiceResult(boolean ok, T value, FlashMessage flash) {
        this.ok = ok;
        this.value = value;
        this.flash = flash;
    }

    public static <T> ServiceResult<T> success(T value, FlashMessage flash) {
        return new ServiceResult<>(true, value, flash);
    }

    public static <T> ServiceResult<T> failure(FlashMessage flash) {
        return new ServiceResult<>(false, null, flash);
    }

    public boolean isOk() {
        return ok;
    }

    public T getValue() {
        return value;
    }

    public FlashMessage getFlash() {
        return flash;
    }
}
