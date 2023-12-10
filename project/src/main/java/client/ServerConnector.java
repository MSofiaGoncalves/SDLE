package client;

import client.model.ShoppingList;
import com.google.gson.Gson;
import org.zeromq.ZContext;
import org.zeromq.ZMQ;

import java.io.FileInputStream;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Handles the connection to the server.
 */
public class ServerConnector {
    private ZContext context;
    private ZMQ.Socket socket;

    private ConcurrentHashMap<String, ZMQ.Socket> nodes;

    /**
     * Creates a zmq socket and connects to the server.
     */
    public ServerConnector() {
        try {
            context = new ZContext();
            socket = context.createSocket(ZMQ.REQ);
            socket.setReqRelaxed(true);
            socket.setSendTimeOut(2 * Integer.parseInt(Session.getSession().getProperty("refreshTime")));
            for (String host : Session.getSession().getProperty("nodes").split(";")) {
                socket.connect(host);
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    /**
     * Sends a request to the server to write a list.
     * @param shoppingList The list to write.
     */
    public void writeList(ShoppingList shoppingList) {
        String request = String.format("{\"method\":\"write\", \"list\":%s}", new Gson().toJson(shoppingList));
        socket.send(request.getBytes(ZMQ.CHARSET), 0);


    }

    /**
     * Sends a request to the server to get a list.
     * @param id The id of the list to get.
     * @return The list with the given id.
     */
    public ShoppingList readList(String id) {
        String request = String.format("{\"method\":\"read\", \"listId\":\"%s\"}", id);
        socket.send(request.getBytes(ZMQ.CHARSET), ZMQ.NOBLOCK);

        ZMQ.Poller poller = context.createPoller(1);
        poller.register(socket, ZMQ.Poller.POLLIN);

        long timeout = 2 * Long.parseLong(Session.getSession().getProperty("refreshTime"));

        int pollResult = poller.poll(timeout);

        if (pollResult == -1 || pollResult == 0) {
            return null;
        } else {
            byte[] reply = socket.recv(0);
            return new Gson().fromJson(new String(reply, ZMQ.CHARSET), ShoppingList.class);
        }
    }
}
