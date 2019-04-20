package ru.ifmo.se.s267880.lab56.shared.communication;

import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.SocketChannel;

public interface Receiver {
    InputStream getInputStream() throws IOException;
    ReadableByteChannel getReadableByteChannel() throws IOException;

    static Receiver fromSocket(Socket socket) {
        return new Receiver() {
            @Override
            public InputStream getInputStream() throws IOException {
                return socket.getInputStream();
            }

            @Override
            public ReadableByteChannel getReadableByteChannel() throws IOException {
                return Channels.newChannel(getInputStream());
            }
        };
    }

    static Receiver fromSocketChannel(SocketChannel socketChannel) {
        return new Receiver() {
            @Override
            public InputStream getInputStream() throws IOException {
                return Channels.newInputStream(socketChannel);
            }

            @Override
            public ReadableByteChannel getReadableByteChannel() throws IOException {
                return socketChannel;
            }
        };
    }
}
