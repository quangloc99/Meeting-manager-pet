package ru.ifmo.se.s267880.lab56.shared.communication;

import ru.ifmo.se.s267880.lab56.client.SocketConnector;
import ru.ifmo.se.s267880.lab56.shared.BoundedInputStream;
import ru.ifmo.se.s267880.lab56.shared.Config;

import java.io.*;
import java.net.Socket;
import java.nio.channels.Channels;
import java.nio.channels.SocketChannel;

public class FileTransferRequest implements Serializable, MessageWithSocket, MessageWithSocketChannel {
    private String destinationFileName;
    private long fileSize;
    private transient InputStream sourceFileInputStream;
    private transient boolean initialized;
    private transient boolean closeFileAfterTransfer;

    public FileTransferRequest(String destinationFileName, File sourceFile) throws FileNotFoundException {
        this(destinationFileName, sourceFile.length(), new FileInputStream(sourceFile));
    }

    public FileTransferRequest(File sourceFile) throws FileNotFoundException {
        this(sourceFile.length(), new FileInputStream(sourceFile));
        this.closeFileAfterTransfer = true;
    }

    public FileTransferRequest(long fileSize, InputStream sourceFileInputStream) {
        this(null, fileSize, sourceFileInputStream);
    }

    public FileTransferRequest(String destinationFileName, long fileSize, InputStream sourceFileInputStream) {
        this.destinationFileName = destinationFileName;
        this.fileSize = fileSize;
        this.sourceFileInputStream = sourceFileInputStream;
        this.initialized = true;
    }

    public File getDestinationFile() throws IOException {
        File res;
        if (destinationFileName == null) {
            res = File.createTempFile(Config.TEMP_FILE_PREFIX, null);
            res.deleteOnExit();
        } else {
            res = new File(destinationFileName);
        }
        destinationFileName = res.getCanonicalPath();
        return res;
    }

    public void afterSent(Socket socket) throws IOException {
        if (!initialized) throw new RuntimeException("This method must be call at the sender side.");
        try {
            synchronized (socket) {
                transfer(socket.getOutputStream(), new BoundedInputStream(sourceFileInputStream, fileSize));
            }
        } finally {
            if (closeFileAfterTransfer) sourceFileInputStream.close();
        }
    }

    public void afterSent(SocketChannel socketChannel) throws IOException {
        if (!initialized) throw new RuntimeException("This method must be call at the sender side.");
        try {
            synchronized (socketChannel) {
                transfer(Channels.newOutputStream(socketChannel), new BoundedInputStream(sourceFileInputStream, fileSize));
            }
        } finally {
            if (closeFileAfterTransfer) sourceFileInputStream.close();
        }
    }

    public void afterReceived(Socket socket) throws IOException{
        if (initialized) throw new RuntimeException("This method must be call at the receiver side.");
        OutputStream des = new FileOutputStream(getDestinationFile());
        synchronized (socket) {
            transfer(des, new BoundedInputStream(socket.getInputStream(), fileSize));
        }
        des.close();
    }

    public void afterReceived(SocketChannel socket) throws IOException {
        if (initialized) throw new RuntimeException("This method must be call at the receiver side.");
        OutputStream des = new FileOutputStream(getDestinationFile());
        synchronized (socket) {
            transfer(des, new BoundedInputStream(Channels.newInputStream(socket), fileSize));
        }
        des.close();
    }

    private void transfer(OutputStream des, InputStream src) throws IOException {
        byte[] buf = new byte[1024];
        int byteRead = 0;
        while ((byteRead = src.read(buf)) != -1) {
            des.write(buf, 0, byteRead);
        }
    }

    public boolean isCloseFileAfterTransfer() {
        return closeFileAfterTransfer;
    }

    public void setCloseFileAfterTransfer(boolean closeFileAfterTransfer) {
        this.closeFileAfterTransfer = closeFileAfterTransfer;
    }
}
