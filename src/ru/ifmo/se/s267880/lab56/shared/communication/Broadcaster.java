package ru.ifmo.se.s267880.lab56.shared.communication;

import ru.ifmo.se.s267880.lab56.shared.EventEmitter;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class Broadcaster<T> implements Runnable {
    private Receiver receiver;
    private final Map<T, EventEmitter<Message<T>>> events = new HashMap<>();
    public final EventEmitter<Exception> onError = new EventEmitter<>();
    private volatile boolean isConnecting = true;

    public Broadcaster(Receiver receiver) {
        this.receiver = receiver;
    }

    @Override
    public void run() {
        try {
            while (true) {
                Message<T> msg = receiver.receive();
                if (!events.containsKey(msg.getType())) continue;
                events.get(msg.getType()).emit(msg);
            }
        } catch (ClassNotFoundException | IOException e) {
            isConnecting = false;
            onError.emit(e);
        }
    }

    public EventEmitter<Message<T>> whenReceive(T event) {
        events.putIfAbsent(event, new EventEmitter<>());
        return events.get(event);
    }

    public void removeAllListeners() {
        events.forEach((key, value) -> value.clear());
        onError.clear();
    }

    public boolean isConnecting() {
        return isConnecting;
    }
}
