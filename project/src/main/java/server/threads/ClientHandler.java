package server.threads;

import server.Store;
import server.connections.NodeConnector;
import server.db.Database;
import server.model.HashRing;
import server.model.ShoppingList;
import com.google.gson.Gson;
import server.model.Message;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
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
        functionMap.put("read", this::readList);

        functionMap.get(message.getMethod()).apply(null);
    }

    /**
     * Write list request from a client. <br>
     * The server either starts a quorum or redirects it to the node that should.
     */
    private Void writeList(Void unused) {
        Store.getLogger().info("List writing request: " + message.getList().getId());

        HashRing ring = Store.getInstance().getHashRing();
        Set<String> nodes = ring.getNodes(message.getList().getId());

        if (nodes.contains(Store.getProperty("nodehost"))) { // Part of Ring
            QuorumHandler quorum = new QuorumHandler(message.getList(), QuorumMode.WRITE);
            quorum.setClientIdentity(identity);
            quorum.run();
        } else { // Not part of ring, redirect
            String node = nodes.iterator().next();
            Store.getLogger().info("Redirecting list write to node: " + node);
            String redirectId = java.util.UUID.randomUUID().toString();
            Store.getInstance().getOngoingRedirects().put(redirectId, identity);
            new NodeConnector(node).sendRedirectWrite(message.getList(), redirectId);
        }

        return null;
    }

    /**
     * Read list request from a client. <br>
     * The server either starts a quorum or redirects it to the node that should.
     */
    private Void readList(Void unused) {
        Store.getLogger().info("List reading request: " + message.getListId());

        HashRing ring = Store.getInstance().getHashRing();
        Set<String> nodes = ring.getNodes(message.getListId());

        if (nodes.contains(Store.getProperty("nodehost"))) { // Part of Ring
            QuorumHandler quorum = new QuorumHandler(message.getListId(), QuorumMode.READ);
            quorum.setClientIdentity(identity);
            quorum.run();
        } else { // Not part of ring, redirect
            String node = nodes.iterator().next();
            Store.getLogger().info("Redirecting list read to node: " + node);
            String redirectId = java.util.UUID.randomUUID().toString();
            Store.getInstance().getOngoingRedirects().put(redirectId, identity);
            new NodeConnector(node).sendRedirectRead(message.getListId(), redirectId);
        }

        return null;
    }
}
