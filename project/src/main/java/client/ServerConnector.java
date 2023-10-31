package client;

import client.model.ShoppingList;
import com.google.gson.Gson;
import org.zeromq.ZContext;
import org.zeromq.ZMQ;

public class ServerConnector {
    private ZContext context;
    private ZMQ.Socket socket;

    public ServerConnector() {
        try {
            context = new ZContext();

            socket = context.createSocket(ZMQ.REQ);
            socket.connect("tcp://localhost:5555");

        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    public void insertList(ShoppingList shoppingList) {
        String request = String.format("{\"method\":\"insert\", \"list\":%s}", new Gson().toJson(shoppingList));
        socket.send(request.getBytes(ZMQ.CHARSET), 0);


        System.out.println("Sending request: ");
        byte[] reply = socket.recv(0);
        System.out.println("reply:"+ new String(reply, ZMQ.CHARSET));
    }

    public ShoppingList getList(String id) {
        String request = String.format("{\"method\":\"get\", \"id\":\"%s\"}", id);
        socket.send(request.getBytes(ZMQ.CHARSET), 0);

        byte[] reply = socket.recv(0);
        return new Gson().fromJson(new String(reply, ZMQ.CHARSET), ShoppingList.class);
    }
}
