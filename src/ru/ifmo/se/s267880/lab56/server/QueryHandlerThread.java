package ru.ifmo.se.s267880.lab56.server;

import ru.ifmo.se.s267880.lab56.shared.CommunicationIOException;
import ru.ifmo.se.s267880.lab56.shared.QueryToServer;
import ru.ifmo.se.s267880.lab56.shared.ResultToClient;
import ru.ifmo.se.s267880.lab56.shared.ResultToClientStatus;

import java.io.*;
import java.net.Socket;
import java.util.function.Consumer;

abstract public class QueryHandlerThread extends Thread {
    private Socket client;
    private ServerCommandController cc;
    private InputStream in;
    private OutputStream out;

    public InputStream getInputStream() {
        return in;
    }

    public OutputStream getOutputStream() {
        return out;
    }

    public Socket getClient() {
        return client;
    }

    public QueryHandlerThread(Socket socket, ServerCommandController cc) throws IOException {
        System.out.printf("Connected to client %s!\n", socket.getInetAddress());
        this.client = socket;
        this.cc = cc;
        this.in = socket.getInputStream();
        this.out = socket.getOutputStream();
    }

    @Override
    public void run() {
        try {
            QueryToServer qr = (QueryToServer) new ObjectInputStream(in).readObject();
            cc.execute(qr, this::onCommandSuccessfulExecuted, this::onErrorWhenExecutingCommand);
        } catch (IOException | ClassNotFoundException e) {
            onDisconnectedToClient(new CommunicationIOException("Cannot read data sent from client.", e));
        }
    }

    private void onCommandSuccessfulExecuted(Object o) {
        sendDataToClient(generateResult(ResultToClientStatus.SUCCESS, (Serializable) o), this, this::onDisconnectedToClient);
    }

    private void onErrorWhenExecutingCommand(Exception e) {
        sendDataToClient(generateResult(ResultToClientStatus.FAIL, (Serializable) e), this, this::onDisconnectedToClient);
    }

    private void sendDataToClient(ResultToClient res, Runnable onSuccess, Consumer<Exception> onError) {
        try {
            new ObjectOutputStream(out).writeObject(res);
            out.flush();
            new Thread(onSuccess).start();
        } catch (IOException e) {
            onError.accept(new CommunicationIOException("Cannot send data to client.", e));
        }
    }

    private void onDisconnectedToClient(Exception e) {
        System.err.println(e.getMessage());
        System.out.printf("Disconnected to client %s.\n", client.getInetAddress());
    }

    abstract ResultToClient generateResult(ResultToClientStatus status, Serializable result);
}
