package server.connections;

import com.google.gson.Gson;
import org.w3c.dom.Node;
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

    public NodeConnector(ZMQ.Socket socket) {
        this.socket = socket;
    }

    public ZMQ.Socket sendListWrite(ShoppingList list, String quorumId) {
        String listJSON = new Gson().toJson(list);
        String request =
                String.format("{\"method\":\"write\", \"quorumId\": %s, \"list\":%s}",
                        quorumId, listJSON);
        socket.send(request.getBytes(ZMQ.CHARSET), 0);
        return socket;
    }

    // TODO
    /*
    public void sendListRead(String id) {
        String request = String.format("{\"method\":\"write\", \"list\":%s}", listJSON);
        socket.send(request.getBytes(ZMQ.CHARSET), 0);
    }

     */

    public ZMQ.Socket sendRedirectWrite(ShoppingList list, byte[] identity) {
        String listJSON = new Gson().toJson(list);
        String request = String.format(
                "{\"method\":\"redirectWrite\", \"clientIdentity\":\"%s\", \"list\":%s}",
                new String(identity, ZMQ.CHARSET), listJSON);
        socket.send(request.getBytes(ZMQ.CHARSET), 0);
        return socket;
    }

    public ZMQ.Socket sendRedirectWriteReply(byte[] identity) {
        String request = String.format(
                "{\"method\":\"redirectWriteReply\", \"clientIdentity\": \"%s\"}",
                identity);
        socket.send(request.getBytes(ZMQ.CHARSET), 0);
        return socket;
    }
}
