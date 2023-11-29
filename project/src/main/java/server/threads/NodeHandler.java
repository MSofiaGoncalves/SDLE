package server.threads;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import org.zeromq.ZMQ;
import server.Store;
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
        System.out.println("msg in handler: " + message);
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
        functionMap.put("quorumAck", this::quorumAck);
        functionMap.put("redirectWriteReply", this::redirectWriteReply);

        if (message == null || message.getMethod() == null) {
            return;
        }

        functionMap.get(message.getMethod()).apply(null);
    }

    private Void writeList(Void unused) {
        Store.getLogger().info("Writing list, " + message.getList().getId() + ", into database.");
        Database.getInstance().insertList(message.getList());
        socket.send(String.format(
                "{\"method\":\"quorumAck\", \"quorumId\":%s}",
                message.getQuorumId()).getBytes(ZMQ.CHARSET), 0);
        return null;
    }

    private Void quorumAck(Void unused) {
        Store store = Store.getInstance();
        Store.getLogger().info("Received quorum ack: " + message.getQuorumId());
        QuorumStatus quorumStatus = store.getQuorums().get(message.getQuorumId());
        System.out.println("quorumStatus: " + quorumStatus);
        if (quorumStatus != null) {
            if (quorumStatus.increment()) {
                store.getQuorums().remove(message.getQuorumId());
            }
        }
        return null;
    }

    private Void redirectWrite(Void unused) {
        Store.getLogger().info("Received list write redirect: " + message.getList().getId());
        QuorumHandler quorum = new QuorumHandler(message.getList(), QuorumMode.WRITE);
        quorum.setNodeSocket(socket);
        System.out.println("REDIRECTING->client identity: " + message.getClientIdentity());
        quorum.setClientIdentity(message.getClientIdentity().getBytes(ZMQ.CHARSET));
        quorum.run();
        return null;
    }

    private Void redirectWriteReply(Void unused) {
        ZMQ.Socket socket =  Store.getInstance().getClientBroker();
        System.out.println("redirectWriteReply: " + message.getClientIdentity().getBytes(ZMQ.CHARSET));

        socket.send(message.getClientIdentity().getBytes(ZMQ.CHARSET), ZMQ.SNDMORE);
        socket.send("".getBytes(), ZMQ.SNDMORE);
        socket.send("", 0);
        return null;
    }
}
