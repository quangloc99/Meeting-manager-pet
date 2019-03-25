package ru.ifmo.se.s267880.lab56.server;

import ru.ifmo.se.s267880.lab56.shared.CommunicationIOException;
import ru.ifmo.se.s267880.lab56.shared.QueryToServer;
import ru.ifmo.se.s267880.lab56.shared.ResultToClient;
import ru.ifmo.se.s267880.lab56.shared.ResultToClientStatus;

import java.io.*;
import java.net.Socket;

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
    }

    @Override
    public void run() {
        try {
            getClientIOStreams();
            while (true) {
                ResultToClient res = null;
                try {
                    QueryToServer qr = (QueryToServer) new ObjectInputStream(in).readObject();
                    res = generateResult(ResultToClientStatus.SUCCESS, (Serializable) cc.execute(qr));
                } catch (EOFException | ClassNotFoundException e) {
                    throw new CommunicationIOException("Cannot read data sent from client.", e);
                } catch (Exception e) {
                    e.printStackTrace();
                    res = generateResult(ResultToClientStatus.FAIL, e);
                }

                try {
                    new ObjectOutputStream(out).writeObject(res);
                    out.flush();
                } catch (IOException e) {
                    throw new CommunicationIOException("Cannot send data to client.", e);
                }
            }
        } catch (CommunicationIOException e) {
            System.err.println(e.getMessage());
            System.out.printf("Disconnected to client %s.\n", client.getInetAddress());
        }
    }

    private void getClientIOStreams() throws CommunicationIOException {
        try {
            in = client.getInputStream();
            out = client.getOutputStream();
        } catch (IOException e) {
            throw new CommunicationIOException("Cannot get I/O stream from client socket.", e);
        }

    }

    abstract ResultToClient generateResult(ResultToClientStatus status, Serializable result);
}
