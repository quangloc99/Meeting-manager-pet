package ru.ifmo.se.s267880.lab56.shared;

import java.util.function.Consumer;

public class HandlerCallback<T> {
    private Consumer<T> onSuccessCallback;
    private Consumer<Exception> onErrorCallback;

    public static <T> HandlerCallback ofSuccessHandler(Consumer<T> onSuccessCallback) {
        HandlerCallback<T> res = new HandlerCallback<>();
        res.onSuccessCallback = onSuccessCallback;
        return res;
    }

    public static HandlerCallback<Void> ofErrorHandler(Consumer<Exception> onErrorCallback) {
        HandlerCallback<Void> res = new HandlerCallback<>();
        res.onErrorCallback = onErrorCallback;
        return res;
    }

    private HandlerCallback() {}
    public HandlerCallback(Consumer<T> onSuccessCallback, Consumer<Exception> onErrorCallback) {
        this.onSuccessCallback = onSuccessCallback;
        this.onErrorCallback = onErrorCallback;
    }

    public void onSuccess(T o) {
        if (onSuccessCallback != null) onSuccessCallback.accept(o);
    }

    public void onError(Exception e) {
        if (onErrorCallback != null) onErrorCallback.accept(e);
    }
}
