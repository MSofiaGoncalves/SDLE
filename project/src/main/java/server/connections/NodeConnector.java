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

    /**
     * Send a list write request to another node. (Quorum)
     * @param list The list to write.
     * @param quorumId The id of the quorum.
     */
    public void sendListWrite(ShoppingList list, String quorumId) {
        String listJSON = new Gson().toJson(list);
        String request =
                String.format("{\"method\":\"write\", \"quorumId\": %s, \"list\":%s}",
                        quorumId, listJSON);
        socket.send(request.getBytes(ZMQ.CHARSET), 0);
    }

    /**
     * Reply to write request. (Quorum)
     * @param quorumId
     */
    public void sendListWriteAck(String quorumId) {
        socket.send(String.format(
                "{\"method\":\"writeAck\", \"quorumId\":%s}",
                quorumId).getBytes(ZMQ.CHARSET), 0);
    }

    /**
     * Redirect a client write request to another node.
     * @param list The list to write.
     * @param redirectId The id of the redirection, to be used in the reply.
     */
    public void sendRedirectWrite(ShoppingList list, String redirectId) {
        String listJSON = new Gson().toJson(list);
        String request = String.format(
                "{\"method\":\"redirectWrite\", \"redirectId\":\"%s\", \"list\":%s}",
                redirectId, listJSON);
        socket.send(request.getBytes(ZMQ.CHARSET), 0);
    }

    /**
     * Reply to a redirect write request.
     * @param redirectId The id of the redirection, so the node can identify the client.
     */
    public void sendRedirectWriteReply(String redirectId) {
        String request = String.format(
                "{\"method\":\"redirectWriteReply\", \"redirectId\": \"%s\"}",
                redirectId);
        socket.send(request.getBytes(ZMQ.CHARSET), 0);
    }

    // TODO
    /*
    public void sendListRead(String id) {
        String request = String.format("{\"method\":\"write\", \"list\":%s}", listJSON);
        socket.send(request.getBytes(ZMQ.CHARSET), 0);
    }

     */
}
