package ru.ifmo.se.s267880.lab56.shared.functional;

import java.util.function.Function;

import static ru.ifmo.se.s267880.lab56.shared.Helper.sneakyThrows;

public interface FunctionWithException<T, R> {
    R apply(T val) throws Exception;
    static <T, R> Function<T, R> toFunction(FunctionWithException<T, R> func) {
        return val -> {
            try {
                return func.apply(val);
            } catch (Exception e) {
                sneakyThrows(e);
                return null;
            }
        };
    }

}
