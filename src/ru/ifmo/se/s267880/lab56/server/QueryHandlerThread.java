package ru.ifmo.se.s267880.lab56.server;

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
        System.err.println("found client!");
        this.client = socket;
        this.cc = cc;
    }

    @Override
    public void run() {
        try (Socket client = this.client) {

            while (true) {
                ResultToClient res = null;
                try {
                    in = client.getInputStream();
                    QueryToServer qr = (QueryToServer) new ObjectInputStream(in).readObject();
                    res = generateResult(ResultToClientStatus.SUCCESS, (Serializable) cc.execute(qr));
                } catch (Exception e) {
                    e.printStackTrace();
                    res = generateResult(ResultToClientStatus.FAIL, e);
                }

                out = client.getOutputStream();
                new ObjectOutputStream(out).writeObject(res);
                out.flush();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    abstract ResultToClient generateResult(ResultToClientStatus status, Serializable result);
}
