package ru.ifmo.se.s267880.lab56.shared.communication;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;

public interface Receiver {
    InputStream getInputStream() throws IOException;
    ReadableByteChannel getReadableByteChannel() throws IOException;
    default <T extends Message> T receive() throws IOException, ClassNotFoundException {
        return receiveWithStream();
    }

    default <T extends Message> T receiveWithStream() throws IOException, ClassNotFoundException {
        @SuppressWarnings("unchecked")
        T res = (T) new ObjectInputStream(getInputStream()).readObject();
        res.afterReceived(this);
        return res;
    }

    default <T extends Message> T receiveWithChannel() throws IOException, ClassNotFoundException {
        return receiveWithStream();   // still has no better way to do this.
    }

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
//
//            @Override
//            public <T extends Message> T receiveWithChannel() throws IOException, ClassNotFoundException {
//                synchronized (socket) { return Receiver.super.receiveWithChannel(); }
//            }
//
//            @Override
//            public <T extends Message> T receiveWithStream() throws IOException, ClassNotFoundException {
//                synchronized (socket) { return Receiver.super.receiveWithStream(); }
//            }
        };
    }

    static Receiver fromSocketChannel(SocketChannel socketChannel) {
        return new Receiver() {
            @Override
            public InputStream getInputStream() throws IOException {
//                return Channels.newInputStream(socketChannel);
//                The above commented code will block the writing process.
//                The code below is a solution from this answer:
//                https://stackoverflow.com/questions/174774/java-are-concurrent-reads-and-writes-possible-on-a-blocking-socketchannel-via-o
                return Channels.newInputStream(new ReadableByteChannel() {
                    public int read(ByteBuffer dst) throws IOException {
                        return socketChannel.read(dst);
                    }
                    public void close() throws IOException {
                        socketChannel.close();
                    }
                    public boolean isOpen() {
                        return socketChannel.isOpen();
                    }
                });
            }

            @Override
            public ReadableByteChannel getReadableByteChannel() throws IOException {
                return socketChannel;
            }

            @Override
            public <T extends Message> T receive() throws IOException, ClassNotFoundException {
                return receiveWithChannel();
            }

            //
//            @Override
//            public <T extends Message> T receiveWithChannel() throws IOException, ClassNotFoundException {
//                synchronized (socketChannel) { return Receiver.super.receiveWithChannel(); }
//            }
//
//            @Override
//            public <T extends Message> T receiveWithStream() throws IOException, ClassNotFoundException {
//                synchronized (socketChannel) { return Receiver.super.receiveWithStream(); }
//            }
        };
    }
}
