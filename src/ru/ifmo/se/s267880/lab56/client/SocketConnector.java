package ru.ifmo.se.s267880.lab56.client;

import ru.ifmo.se.s267880.lab56.shared.EventEmitter;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SocketChannel;
import java.nio.channels.UnresolvedAddressException;

/**
 * A class that handle stuffs related to Socket.
 */
public class SocketConnector {
    public final EventEmitter<SocketChannel> connectSucessfulEvent = new EventEmitter<>();
    public final EventEmitter<Exception> errorEvent = new EventEmitter<>();
    private int time;
    private long delayTime;

    public SocketConnector(int time, long delayTime) {
        this.time = time;
        this.delayTime = delayTime;
    }

    public void tryConnectTo(InetSocketAddress address) {
        new Thread(() -> {
            try {
                for (int i = time; i > 0; --i) {
                    try {
                        Thread.sleep(100);
                        SocketChannel result = SocketChannel.open(address);
                        connectSucessfulEvent.emit(result);
                        return ;
                    } catch (UnresolvedAddressException e) {
                        errorEvent.emit(e);
                    } catch (IOException e) {
                        System.err.printf("Unable to connect to %s. Trying to connect %d more times.\n", address, i);
                        Thread.sleep(delayTime);
                    }
                }
                errorEvent.emit(new NullPointerException());
            } catch (InterruptedException e) {
                errorEvent.emit(e);
            }
        }).start();
    }

    public int getTime() { return time; }
    public long getDelayTime() { return delayTime; }

    public void setTime(int time) { this.time = time; }
    public void setDelayTime(long delayTime) { this.delayTime = delayTime; }
}
