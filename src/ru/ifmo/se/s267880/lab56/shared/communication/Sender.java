package ru.ifmo.se.s267880.lab56.shared.communication;

import ru.ifmo.se.s267880.lab56.shared.Helper;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.SocketChannel;
import java.nio.channels.WritableByteChannel;

public interface Sender {
    OutputStream getOutputStream() throws IOException;
    WritableByteChannel getWritableByteChannel() throws IOException;

    default void send(Message msg) throws IOException {
        sendWithStream(msg);
    }

    default void sendWithStream(Message msg) throws IOException {
        final OutputStream out = getOutputStream();
        new ObjectOutputStream(out).writeObject(msg);
        out.flush();
        msg.afterSent(this);
    }

    default void sendWithChannel(Message msg) throws IOException {
        ByteBuffer bf = ByteBuffer.wrap(Helper.serializableToByteArray(msg));
        getWritableByteChannel().write(bf);
        msg.afterSent(this);
    }

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

            @Override
            public void send(Message msg) throws IOException {
                sendWithChannel(msg);
            }
        };
    }
}
