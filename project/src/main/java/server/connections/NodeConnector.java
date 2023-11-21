package server.connections;

import com.google.gson.Gson;
import org.zeromq.ZMQ;
import server.Store;
import server.db.Database;
import server.model.ShoppingList;

/**
 * Handles outgoing connections to other nodes.
 */
public class NodeConnector {
    private String address;
    private ZMQ.Socket socket;

    /**
     * Creates a new NodeConnector.
     *
     * @param address The address of the node to connect to.
     */
    public NodeConnector(String address) {
        this.address = address;

        socket = Store.getInstance().getNodes().get(address);
        if (socket == null) {
            Store.getLogger().severe("Could not find socket for node: " + address);
        }
    }

    public void sendList(String listJSON) {
        String request = String.format("{\"method\":\"insert\", \"list\":%s}", listJSON);
        socket.send(request.getBytes(ZMQ.CHARSET), 0);
    }
}
