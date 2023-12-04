package server.connections;

import com.google.gson.Gson;
import org.w3c.dom.Node;
import org.zeromq.ZMQ;
import server.Store;
import server.db.Database;
import server.model.Message;
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

    /*************** Write ****************/

    /**
     * Send a list write request to another node. (Quorum)
     * @param list The list to write.
     * @param quorumId The id of the quorum.
     */
    public void sendListWrite(ShoppingList list, String quorumId) {
        Message message = new Message();
        message.setMethod("write");
        message.setQuorumId(quorumId);
        message.setList(list);
        sendMessage(message);
    }

    /**
     * Reply to write request. (Quorum)
     * @param quorumId
     */
    public void sendListWriteAck(String quorumId) {
        Message message = new Message();
        message.setMethod("writeAck");
        message.setQuorumId(quorumId);
        sendMessage(message);
    }

    /**
     * Redirect a client write request to another node.
     * @param list The list to write.
     * @param redirectId The id of the redirection, to be used in the reply.
     */
    public void sendRedirectWrite(ShoppingList list, String redirectId) {
        Message message = new Message();
        message.setMethod("redirectWrite");
        message.setRedirectId(redirectId);
        message.setList(list);
        sendMessage(message);
    }

    /**
     * Reply to a redirect write request.
     * @param redirectId The id of the redirection, so the node can identify the client.
     */
    public void sendRedirectWriteReply(String redirectId) {
        Message message = new Message();
        message.setMethod("redirectWriteReply");
        message.setRedirectId(redirectId);
        sendMessage(message);
    }

    /*************** Read ****************/

    /**
     * Send a list read request to another node. (Quorum)
     * @param id The list to read.
     * @param quorumId The id of the quorum.
     */
    public void sendListRead(String id, String quorumId) {
        Message message = new Message();
        message.setMethod("read");
        message.setQuorumId(quorumId);
        message.setListId(id);
        sendMessage(message);
    }

    /**
     * Reply to read request. (Quorum)
     * @param list The list read.
     * @param quorumId
     */
    public void sendListReadAck(ShoppingList list, String quorumId) {
        Message message = new Message();
        message.setMethod("readAck");
        message.setQuorumId(quorumId);
        message.setList(list);
        sendMessage(message);
    }

    /**
     * Redirect a client read request to another node.
     * @param id The id of the list to write.
     * @param redirectId The id of the redirection, to be used in the reply.
     */
    public void sendRedirectRead(String id, String redirectId) {
        Message message = new Message();
        message.setMethod("redirectRead");
        message.setRedirectId(redirectId);
        message.setListId(id);
        sendMessage(message);
    }

    /**
     * Reply to a redirect write request.
     * @param redirectId The id of the redirection, so the node can identify the client.
     */
    public void sendRedirectReadReply(ShoppingList list, String redirectId) {
        Message message = new Message();
        message.setMethod("redirectReadReply");
        message.setRedirectId(redirectId);
        message.setList(list);
        sendMessage(message);
    }

    /**
     * Send a status update to another node.
     * @param id The id of the node to update.
     * @param status Current status of the node. True if online, false otherwise.
     */
    public void sendStatusUpdate(String id, boolean status) {
        String request = String.format(
                "{\"method\":\"statusUpdate\", \"id\": \"%s\", \"status\":%s}",
                id, status);
        socket.send(request.getBytes(ZMQ.CHARSET), 0);
    }

    private void sendMessage(Message message) {
        message.setAuthorAddress(Store.getProperty("nodehost"));
        String request = new Gson().toJson(message);
        socket.send(request.getBytes(ZMQ.CHARSET), 0);
        //Store.getInstance().getWaitingReply().put(message.getId(), socket);
    }
}
