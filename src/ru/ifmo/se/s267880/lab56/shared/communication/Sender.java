package ru.ifmo.se.s267880.lab56.shared.communication;

import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.channels.Channels;
import java.nio.channels.SocketChannel;
import java.nio.channels.WritableByteChannel;

public interface Sender {
    OutputStream getOutputStream() throws IOException;
    WritableByteChannel getWritableByteChannel() throws IOException;

    static Sender fromSocket(Socket socket) {
        return new Sender() {
            @Override
            public OutputStream getOutputStream() throws IOException {
                return socket.getOutputStream();
            }

            @Override
            public WritableByteChannel getWritableByteChannel() throws IOException {
                return Channels.newChannel(getOutputStream());
            }
        };
    }

    static Sender fromSocketChannel(SocketChannel socketChannel) {
        return new Sender() {
            @Override
            public OutputStream getOutputStream() throws IOException {
                return Channels.newOutputStream(getWritableByteChannel());
            }

            @Override
            public WritableByteChannel getWritableByteChannel() throws IOException {
                return socketChannel;
            }
        };
    }
}
