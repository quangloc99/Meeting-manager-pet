package ru.ifmo.se.s267880.lab56.server;

import ru.ifmo.se.s267880.lab56.shared.QueryToServer;
import ru.ifmo.se.s267880.lab56.shared.ResultToClient;
import ru.ifmo.se.s267880.lab56.shared.ResultToClientStatus;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.Socket;

public class QueryHandlerThread extends Thread {
    private Socket client;
    private ServerQueryCommandController cc;
    public QueryHandlerThread(Socket socket, ServerQueryCommandController cc) {
        System.err.println("found client!");
        this.client = socket;
        this.cc = cc;
    }

    @Override
    public void run() {
        try (Socket client = this.client) {
            ObjectInputStream in = new ObjectInputStream(client.getInputStream());
            ObjectOutputStream out = new ObjectOutputStream(client.getOutputStream());

            ResultToClient res = null;
            try {
                QueryToServer qr = (QueryToServer) in.readObject();
                synchronized (cc) {
                    res = new ResultToClient(ResultToClientStatus.SUCCESS, (Serializable) cc.execute(qr));
                }
            } catch (Exception e) {
                e.printStackTrace();
                res = new ResultToClient(ResultToClientStatus.FAIL, e);
            }


            out.writeObject(res);
            out.flush();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
