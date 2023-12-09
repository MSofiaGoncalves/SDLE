package server.connections;

import com.google.gson.Gson;
import server.Store;
import server.model.Message;
import server.model.ShoppingList;

import java.time.Instant;
import java.util.List;

/**
 * Handles outgoing connections to other nodes.
 */
public class NodeConnector {
    private String address;

    /**
     * Creates a new NodeConnector.
     *
     * @param address The address of the node to connect to.
     */
    public NodeConnector(String address) {
        this.address = address;
    }

    /*************** Write ****************/

    /**
     * Send a list write request to another node. (Quorum)
     *
     * @param list     The list to write.
     * @param quorumId The id of the quorum.
     */
    public void sendListWrite(ShoppingList list, String quorumId) {
        Message message = new Message();
        message.setMethod("write");
        message.setQuorumId(quorumId);
        message.setList(list);
        sendMessage(message);
        setWaitingReply();
    }

    /**
     * Reply to write request. (Quorum)
     *
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
     *
     * @param list       The list to write.
     * @param redirectId The id of the redirection, to be used in the reply.
     */
    public void sendRedirectWrite(ShoppingList list, String redirectId) {
        Message message = new Message();
        message.setMethod("redirectWrite");
        message.setRedirectId(redirectId);
        message.setList(list);
        sendMessage(message);
        setWaitingReply(true);
    }

    /**
     * Reply to a redirect write request.
     *
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
     *
     * @param id       The list to read.
     * @param quorumId The id of the quorum.
     */
    public void sendListRead(String id, String quorumId) {
        Message message = new Message();
        message.setMethod("read");
        message.setQuorumId(quorumId);
        message.setListId(id);
        sendMessage(message);
        setWaitingReply();
    }

    /**
     * Reply to read request. (Quorum)
     *
     * @param list     The list read.
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
     *
     * @param id         The id of the list to write.
     * @param redirectId The id of the redirection, to be used in the reply.
     */
    public void sendRedirectRead(String id, String redirectId) {
        Message message = new Message();
        message.setMethod("redirectRead");
        message.setRedirectId(redirectId);
        message.setListId(id);
        sendMessage(message);
        setWaitingReply(true);
    }

    /**
     * Reply to a redirect write request.
     *
     * @param redirectId The id of the redirection, so the node can identify the client.
     */
    public void sendRedirectReadReply(ShoppingList list, String redirectId) {
        Message message = new Message();
        message.setMethod("redirectReadReply");
        message.setRedirectId(redirectId);
        message.setList(list);
        sendMessage(message);
    }

    /*************** Status ****************/

    /**
     * Send a status update to another node.
     *
     * @param address The address of the node to update.
     * @param status  Current status of the node. True if online, false otherwise.
     */
    public void sendStatusUpdate(String address, boolean status) {
        Message message = new Message();
        message.setMethod("statusUpdate");
        message.setStatusNodeAddress(address);
        message.setStatusValue(status);
        sendMessage(message);
    }

    /**
     * Sent list of lists to hold while <strong>address</strong> is offline
     *
     * @param address The address of the sick node.
     * @param lists   The lists to hold.
     */
    public void sendHintedHandoff(String address, List<ShoppingList> lists) {
        Message message = new Message();
        message.setMethod("hintedHandoff");
        message.setAddress(address);
        message.setLists(lists);
        sendMessage(message);
    }

    /**
     * Return hinted lists to owner.
     *
     * @param lists The lists to return.
     */
    public void sendReturnHinted(List<ShoppingList> lists) {
        Message message = new Message();
        message.setMethod("returnHinted");
        message.setLists(lists);
        sendMessage(message);
    }

    /**
     * Sent list of lists to be relocated
     *
     * @param lists   The lists to transfer.
     */
    public void sendHandoff(List<ShoppingList> lists) {
        Message message = new Message();
        message.setMethod("handoff");
        message.setLists(lists);
        sendMessage(message);
    }

    /**
     * Send a heartbeat to another node.
     */
    public void sendHeartbeat() {
        Message message = new Message();
        message.setMethod("heartbeat");
        sendMessage(message);
    }

    /**
     * Send a heartbeat reply to another node.
     */
    public void sendHeartbeatReply() {
        Message message = new Message();
        message.setMethod("heartbeatReply");
        sendMessage(message);
    }

    /**
     * Send a message to the node's socket.
     *
     * @param message The message to send.
     */
    private void sendMessage(Message message) {
        String request = new Gson().toJson(message);
        Store.getInstance().sendNodeMessage(address, request);
    }

    /**
     * Set the node as waiting for a reply. <br>
     * This is used to detect failure detection on that node.
     * @param redirect True if the request was redirected, false otherwise.
     */
    private void setWaitingReply(boolean redirect) {
        Store.getInstance().getWaitingReply().put(address,
                Instant.now().plusMillis(
                        !redirect ? 0
                                : Integer.parseInt(Store.getProperty("failureTimeout"))
                ));
    }

    private void setWaitingReply() {
        setWaitingReply(false);
    }
}
