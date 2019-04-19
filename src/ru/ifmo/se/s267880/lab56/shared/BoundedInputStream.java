package ru.ifmo.se.s267880.lab56.shared;

import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;

public class BoundedInputStream extends InputStream {
    private InputStream in;
    private long length;

    public BoundedInputStream(InputStream in, long length) {
        Objects.requireNonNull(in);
        this.in = in;
        this.length = length;
    }

    @Override
    public int read() throws IOException  {
        if (length <= 0) return -1;
        --length;
        return in.read();
    }

    @Override
    public int read(byte[] buffer) throws IOException {
        return read(buffer, 0, buffer.length);
    }

    @Override
    public int read(byte[] buffer, int offset, int size) throws IOException {
        if (length <= 0) return -1;
        size = (int)Long.min(size, length);
        int byteRead = in.read(buffer, offset, size);
        if (byteRead == -1) return -1;
        length -= byteRead;
        return byteRead;
    }
}
