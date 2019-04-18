package ru.ifmo.se.s267880.lab56.client;

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

    public void tryConnectTo(InetSocketAddress address) {
        new Thread(() -> {
            try {
                for (int i = time; i > 0; --i) {
                    try {
                        Thread.sleep(100);
                        SocketChannel result = SocketChannel.open(address);
                        onConnectSuccessfulEvent(result);
                        return ;
                    } catch (UnresolvedAddressException e) {
                        onError(e);
                        return ;
                    } catch (IOException e) {
                        System.err.printf("Unable to connect to %s. Trying to connect %d more times.\n", address, i);
                        Thread.sleep(delayTime);
                    }
                }
                onError(new NullPointerException());
            } catch (InterruptedException e) {
                onError(e);
            }
        }).start();
    }

    public int getTime() { return time; }
    public long getDelayTime() { return delayTime; }

    public void setTime(int time) { this.time = time; }
    public void setDelayTime(long delayTime) { this.delayTime = delayTime; }

    public void onConnectSuccessfulEvent(SocketChannel sc) {
        // nothing. Can be extended
    }

    public void onError(Exception e) {
        // nothing. Can be extended
    }
}
