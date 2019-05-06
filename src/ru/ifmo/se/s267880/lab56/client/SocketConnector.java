package ru.ifmo.se.s267880.lab56.client;

import ru.ifmo.se.s267880.lab56.shared.functional.HandlerCallback;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SocketChannel;
import java.nio.channels.UnresolvedAddressException;

/**
 * A class that handle stuffs related to Socket.
 */
public class SocketConnector {
    private int time;
    private long delayTime;

    public SocketConnector(int time, long delayTime) {
        this.time = time;
        this.delayTime = delayTime;
    }

    public void tryConnectTo(InetSocketAddress address, HandlerCallback<SocketChannel> callback) {
        new Thread(() -> {
            try {
                for (int i = time; i > 0; --i) {
                    try {
                        Thread.sleep(100);
                        SocketChannel result = SocketChannel.open(address);
                        callback.onSuccess(result);
                        return ;
                    } catch (UnresolvedAddressException e) {
                        callback.onError(e);
                        return ;
                    } catch (IOException e) {
                        System.err.printf("Unable to connect to %s. Trying to connect %d more times.\n", address, i);
                        Thread.sleep(delayTime);
                    }
                }
                callback.onError(new NullPointerException());
            } catch (InterruptedException e) {
                callback.onError(e);
            }
        }).start();
    }

    public int getTime() { return time; }
    public long getDelayTime() { return delayTime; }

    public void setTime(int time) { this.time = time; }
    public void setDelayTime(long delayTime) { this.delayTime = delayTime; }
}
