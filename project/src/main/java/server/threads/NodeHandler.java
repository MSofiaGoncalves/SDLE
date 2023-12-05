package server.threads;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import org.zeromq.ZMQ;
import server.Store;
import server.connections.NodeConnector;
import server.db.Database;
import server.model.Message;
import server.model.QuorumStatus;
import server.model.ShoppingList;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * Handles messages from the nodes.
 */
public class NodeHandler implements Runnable {
    private String address;
    private String messageRaw;
    private Message message;

    /**
     * Creates a new NodeHandler.
     * @param address The address of the node that sent the message.
     * @param message The message to handle.
     */
    public NodeHandler(String address, String message) {
        this.messageRaw = message;
        try {
            this.message = new Gson().fromJson(messageRaw, Message.class);
        }
        catch (JsonSyntaxException e) { // message meant for other thread, ignore
            Store.getLogger().warning("Received invalid message: " + messageRaw);
            return;
        }

        this.address = address;
    }

    @Override
    public void run() {
        Map<String, Function<Void, Void>> functionMap = new HashMap<>();
        functionMap.put("write", this::writeList);
        functionMap.put("writeAck", this::writeAck);
        functionMap.put("redirectWrite", this::redirectWrite);
        functionMap.put("redirectWriteReply", this::redirectWriteReply);

        functionMap.put("read", this::readList);
        functionMap.put("readAck", this::readAck);
        functionMap.put("redirectRead", this::redirectRead);
        functionMap.put("redirectReadReply", this::redirectReadReply);

        functionMap.put("statusUpdate", this::statusUpdate);
        functionMap.put("hintedHandoff", this::hintedHandoff);

        if (message == null || message.getMethod() == null) {
            return;
        }

        if (functionMap.get(message.getMethod()) == null) {
            Store.getLogger().warning("Received invalid message: " + messageRaw);
            return;
        }

        functionMap.get(message.getMethod()).apply(null);
    }

    /*************** Write ****************/

    /**
     * List write request from another node. (Quorum) <br>
     * Writes the list to the db and sends an ACK.
     */
    private Void writeList(Void unused) {
        Store.getLogger().info("Writing list, " + message.getList().getId() + ", into database.");
        Database.getInstance().insertList(message.getList());
        new NodeConnector(address).sendListWriteAck(message.getQuorumId());
        return null;
    }

    /**
     * Receive write ack from another node. (Quorum) <br>
     * Update quorum status on the store.
     */
    private Void writeAck(Void unused) {
        processResponse();
        Store store = Store.getInstance();
        Store.getLogger().info("Received write ack: " + message.getQuorumId());
        QuorumStatus quorumStatus = store.getQuorums().get(message.getQuorumId());
        if (quorumStatus != null) {
            if (quorumStatus.increment()) {
                store.getQuorums().remove(message.getQuorumId());
            }
        }
        return null;
    }

    /**
     * Receive a redirect write request from another node. <br>
     * Inits a quorum.
     */
    private Void redirectWrite(Void unused) {
        Store.getLogger().info("Received list write redirect: " + message.getList().getId());
        QuorumHandler quorum = new QuorumHandler(message.getList(), QuorumMode.WRITE);
        quorum.setNodeAddress(address);
        quorum.setRedirectId(message.getRedirectId());
        quorum.run();
        return null;
    }

    /**
     * Handles a redirect reply. <br>
     * Sends a reply to the client.
     */
    private Void redirectWriteReply(Void unused) {
        processResponse();
        Store store = Store.getInstance();
        ZMQ.Socket socket =  store.getClientBroker();

        byte[] clientIdentity = store.getOngoingRedirects().get(message.getRedirectId());
        store.getOngoingRedirects().remove(message.getRedirectId());

        socket.send(clientIdentity, ZMQ.SNDMORE);
        socket.send("".getBytes(), ZMQ.SNDMORE);
        socket.send("", 0);
        return null;
    }

    /*************** Read ****************/

    /**
     * List read request from another node. (Quorum) <br>
     * Reads the list from the db and sends it.
     */
    private Void readList(Void unused) {
        Store.getLogger().info("Reading list, " + message.getListId() + " from database.");
        ShoppingList list = Database.getInstance().readList(message.getListId());
        new NodeConnector(address).sendListReadAck(list, message.getQuorumId());
        return null;
    }

    /**
     * Receive read ack from another node. (Quorum) <br>
     * Update quorum status on the store.
     */
    private Void readAck(Void unused) {
        processResponse();
        Store store = Store.getInstance();
        Store.getLogger().info("Received read ack: " + message.getQuorumId());
        QuorumStatus quorumStatus = store.getQuorums().get(message.getQuorumId());
        if (quorumStatus != null) {
            quorumStatus.addList(message.getList());
            if (quorumStatus.increment()) {
                store.getQuorums().remove(message.getQuorumId());
            }
        }
        return null;
    }

    /**
     * Receive a redirect read request from another node. <br>
     * Inits a quorum.
     */
    private Void redirectRead(Void unused) {
        Store.getLogger().info("Received list read redirect: " + message.getListId());
        QuorumHandler quorum = new QuorumHandler(message.getListId(), QuorumMode.READ);
        quorum.setNodeAddress(address);
        quorum.setRedirectId(message.getRedirectId());
        quorum.run();
        return null;
    }

    /**
     * Handles a read redirect reply. <br>
     * Sends a reply to the client.
     */
    private Void redirectReadReply(Void unused) {
        processResponse();
        Store store = Store.getInstance();
        ZMQ.Socket socket =  store.getClientBroker();

        byte[] clientIdentity = store.getOngoingRedirects().get(message.getRedirectId());
        store.getOngoingRedirects().remove(message.getRedirectId());

        String listJSON = new Gson().toJson(message.getList());
        socket.send(clientIdentity, ZMQ.SNDMORE);
        socket.send("".getBytes(), ZMQ.SNDMORE);
        socket.send(listJSON, 0);
        return null;
    }

    /*************** Status ****************/

    private Void statusUpdate(Void unused) {
        Store.getLogger().info("Received status update about node: " + message.getStatusNodeAddress());
        Store.getInstance().getHashRing().updateNodeStatus(message.getStatusNodeAddress(), message.getStatusValue());
        return null;
    }

    private Void hintedHandoff(Void unused) {
        Store.getLogger().info("Received hinted handoff from node: " + message.getAddress());
        for (ShoppingList list : message.getLists()) {
            Database.getInstance().insertList(list);
        }
        return null;
    }

    private void processResponse() {
        Store.getInstance().getWaitingReply().remove(address);
    }
}
