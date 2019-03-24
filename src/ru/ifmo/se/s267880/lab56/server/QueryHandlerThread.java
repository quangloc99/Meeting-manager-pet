package ru.ifmo.se.s267880.lab56.server;

import ru.ifmo.se.s267880.lab56.shared.QueryToServer;
import ru.ifmo.se.s267880.lab56.shared.ResultToClient;
import ru.ifmo.se.s267880.lab56.shared.ResultToClientStatus;

import java.io.ObjectInputStream;
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
        try {
            //        PrintWriter out = new PrintWriter(client.getOutputStream(), true);
            ObjectInputStream in = new ObjectInputStream(client.getInputStream());
            QueryToServer qr = (QueryToServer) in.readObject();
            ResultToClient res = null;
            try {
                res = new ResultToClient(ResultToClientStatus.SUCCESS, (Serializable) cc.execute(qr));
            } catch (Exception e) {
                e.printStackTrace();
                res = new ResultToClient(ResultToClientStatus.FAIL, e);
            }

            System.out.println(res.getStatus());  // testing

            in.close();
            //        out.close();
            client.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
