package server.threads;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import org.zeromq.ZMQ;
import server.Store;
import server.connections.NodeConnector;
import server.db.Database;
import server.model.Message;
import server.model.QuorumStatus;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * Handles messages from the nodes.
 */
public class NodeHandler implements Runnable {
    private ZMQ.Socket socket;
    private String messageRaw;
    private Message message;

    /**
     * Creates a new NodeHandler.
     * @param socket The socket to send replies to.
     * @param message The message to handle.
     */
    public NodeHandler(ZMQ.Socket socket, String message) {
        this.messageRaw = message;
        try {
            this.message = new Gson().fromJson(messageRaw, Message.class);
        }
        catch (JsonSyntaxException e) { // message meant for other thread, ignore
            Store.getLogger().warning("Received invalid message: " + messageRaw);
            return;
        }

        this.socket = socket;
    }

    @Override
    public void run() {
        Map<String, Function<Void, Void>> functionMap = new HashMap<>();
        functionMap.put("write", this::writeList);
        functionMap.put("redirectWrite", this::redirectWrite);
        functionMap.put("writeAck", this::writeAck);
        functionMap.put("redirectWriteReply", this::redirectWriteReply);

        if (message == null || message.getMethod() == null) {
            return;
        }

        functionMap.get(message.getMethod()).apply(null);
    }

    /**
     * List write request from another node. (Quorum) <br>
     * Writes the list to the db and sends an ACK.
     */
    private Void writeList(Void unused) {
        Store.getLogger().info("Writing list, " + message.getList().getId() + ", into database.");
        Database.getInstance().insertList(message.getList());
        new NodeConnector(socket).sendListWriteAck(message.getQuorumId());
        return null;
    }

    /**
     * Receive write ack from another node. (Quorum) <br>
     * Update quorum status on the store.
     */
    private Void writeAck(Void unused) {
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
        quorum.setNodeSocket(socket);
        quorum.setRedirectId(message.getRedirectId());
        quorum.run();
        return null;
    }

    /**
     * Handles a redirect reply. <br>
     * Sends a reply to the client.
     */
    private Void redirectWriteReply(Void unused) {
        Store store = Store.getInstance();
        ZMQ.Socket socket =  store.getClientBroker();

        byte[] clientIdentity = store.getOngoingRedirects().get(message.getRedirectId());
        store.getOngoingRedirects().remove(message.getRedirectId());

        socket.send(clientIdentity, ZMQ.SNDMORE);
        socket.send("".getBytes(), ZMQ.SNDMORE);
        socket.send("", 0);
        return null;
    }
}
