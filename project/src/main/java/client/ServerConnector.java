package client;

import client.model.ShoppingList;
import com.google.gson.Gson;
import org.zeromq.ZContext;
import org.zeromq.ZMQ;

/**
 * Handles the connection to the server.
 */
public class ServerConnector {
    private ZContext context;
    private ZMQ.Socket socket;

    /**
     * Creates a zmq socket and connects to the server.
     */
    public ServerConnector() {
        try {
            context = new ZContext();

            socket = context.createSocket(ZMQ.REQ);
            socket.connect("tcp://localhost:5555");

        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    /**
     * Sends a request to the server to insert a list.
     * @param shoppingList The list to insert.
     */
    public void insertList(ShoppingList shoppingList) {
        String request = String.format("{\"method\":\"insert\", \"list\":%s}", new Gson().toJson(shoppingList));
        socket.send(request.getBytes(ZMQ.CHARSET), 0);

        byte[] reply = socket.recv(0);
    }

    /**
     * Sends a request to the server to get a list.
     * @param id The id of the list to get.
     * @return The list with the given id.
     */
    public ShoppingList getList(String id) {
        String request = String.format("{\"method\":\"get\", \"id\":\"%s\"}", id);
        socket.send(request.getBytes(ZMQ.CHARSET), 0);

        byte[] reply = socket.recv(0);
        return new Gson().fromJson(new String(reply, ZMQ.CHARSET), ShoppingList.class);
    }
}
