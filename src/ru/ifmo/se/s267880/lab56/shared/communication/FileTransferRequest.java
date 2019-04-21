package ru.ifmo.se.s267880.lab56.shared.communication;

import ru.ifmo.se.s267880.lab56.shared.BoundedInputStream;
import ru.ifmo.se.s267880.lab56.shared.Config;
import ru.ifmo.se.s267880.lab56.shared.Helper;

import java.io.*;

public class FileTransferRequest implements Serializable, Message<MessageType> {
    private String destinationFileName;
    private long fileSize;
    private transient InputStream sourceFileInputStream;
    private transient boolean initialized;
    private transient boolean closeFileAfterTransfer;

    @Override
    public MessageType getType() { return MessageType.REQUEST; }

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
        File res = destinationFileName == null ? Helper.createTempFile() : new File(destinationFileName);
        destinationFileName = res.getCanonicalPath();
        return res;
    }

    @Override
    public void afterSent(Sender sender) throws IOException {
        if (!initialized) throw new RuntimeException("This method must be call at the sender side.");
        try {
            synchronized (sender) {
                transfer(sender.getOutputStream(), new BoundedInputStream(sourceFileInputStream, fileSize));
            }
        } finally {
            if (closeFileAfterTransfer) sourceFileInputStream.close();
        }
    }

    @Override
    public void afterReceived(Receiver receiver) throws IOException{
        if (initialized) throw new RuntimeException("This method must be call at the receiver side.");
        OutputStream des = new FileOutputStream(getDestinationFile());
        synchronized (receiver) {
            transfer(des, new BoundedInputStream(receiver.getInputStream(), fileSize));
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
