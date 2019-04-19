package ru.ifmo.se.s267880.lab56.shared.functional;

import java.util.function.Consumer;

import static ru.ifmo.se.s267880.lab56.shared.Helper.sneakyThrows;

public interface ConsumerWithException<T> {
    void accept(T val) throws Exception;
    static <T> Consumer<T> toConsumer(ConsumerWithException<T> consumer) {
        return val -> {
            try {
                consumer.accept(val);
            } catch (Exception e) {
                sneakyThrows(e);
            }
        };
    }

}
