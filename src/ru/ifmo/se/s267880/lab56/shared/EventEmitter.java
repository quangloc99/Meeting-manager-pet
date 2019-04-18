package ru.ifmo.se.s267880.lab56.shared;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Consumer;

public class EventEmitter <T> {
    private List<Consumer<T>> listeners = Collections.synchronizedList(new LinkedList<>());
    public EventEmitter() {}

    public Consumer<T> listen(Consumer<T> listener) {
        listeners.add(listener);
        return listener;
    }

    public boolean removeListener(Consumer<T> listener) {
        return listeners.remove(listener);
    }

    public void emit(T message) {
        listeners.forEach(l -> l.accept(message));
    }

    public void clear() {
        listeners.clear();
    }
}
