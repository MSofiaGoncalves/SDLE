package server.connections;

import server.Store;
import server.db.Database;
import server.model.HashRing;
import server.model.ShoppingList;
import com.google.gson.Gson;
import org.zeromq.ZMQ;
import server.model.Message;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * Handles messages from the client. <br>
 *
 * Runs as a thread and should be called when a message is received.
 */
public class ClientHandler implements Runnable {
    private byte[] identity;
    private String messageRaw;
    private Message message;

    /**
     * Creates a new ClientHandler.
     * @param identity The identity of the client.
     * @param message The message to handle.
     */
    public ClientHandler(byte[] identity, String message) {
        this.identity = identity;
        this.messageRaw = message;
    }

    @Override
    public void run() {
        message = new Gson().fromJson(messageRaw, Message.class);

        Map<String, Function<Void, Void>> functionMap = new HashMap<>();
        functionMap.put("write", this::writeList);
        functionMap.put("get", this::getList);

        functionMap.get(message.getMethod()).apply(null);
    }

    /**
     * Inserts a list into the database.
     */
    private Void writeList(Void unused) {
        Store.getLogger().info("List writing request: " + message.getList().getId());

        HashRing ring = Store.getInstance().getHashRing();
        String[] nodes = ring.getNodes(message.getList().getId());

        if (nodes[0].equals(Store.getProperty("nodehost"))) { // Quorum Leader
            QuorumHandler quorum = new QuorumHandler(message.getList(), QuorumOperation.WRITE);
            quorum.setClientIdentity(identity);
            quorum.run();
        } else { // Not Quorum Leader, redirect to leader
            Store.getLogger().info("Redirecting list to node: " + nodes[0]);
            new NodeConnector(nodes[0]).sendRedirectWrite(message.getList());
            // TODO replies
            ZMQ.Socket socket =  Store.getInstance().getClientBroker();

            socket.send(identity, ZMQ.SNDMORE);
            socket.send("".getBytes(), ZMQ.SNDMORE);
            socket.send("".getBytes(), 0);
        }

        return null;
    }

    /**
     * Gets a list from the database.
     */
    private Void getList(Void unused) {
        ShoppingList list = Database.getInstance().getList(message.getId());
        // TODO reading
        //reply(new Gson().toJson(list));
        return null;
    }
}
