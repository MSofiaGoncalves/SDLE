package server.model;

import org.zeromq.ZMQ;
import server.Store;
import server.connections.NodeConnector;

/**
 * Keeps track of the status of a quorum.
 */
public class QuorumStatus {
    private final int quorumSize;
    private int currentSize;
    private final String id;
    private final ZMQ.Socket nodeSocket; // for redirect
    private final byte[] identity; // for client responses
    private final String redirectId;
    private ShoppingList list; // used for reads

    /**
     * Creates a new QuorumStatus. <br>
     * Either {identity} or {nodeSocket, redirectId} must be set, for client and redirect replies, respectively.
     * @param quorumSize Number of nodes needed for completion.
     * @param nodeSocket The socket of the node to redirect to.
     * @param identity The identity of the client to reply to.
     * @param redirectId The id of the redirection, to be used in the reply.
     */
    public QuorumStatus(int quorumSize, ZMQ.Socket nodeSocket, byte[] identity, String redirectId) {
        this.quorumSize = quorumSize;
        this.currentSize = 0;
        this.id = java.util.UUID.randomUUID().toString();
        if ((nodeSocket == null && redirectId == null) && identity == null) {
            throw new IllegalArgumentException("Either nodeSocket or identity must be set.");
        }
        this.nodeSocket = nodeSocket;
        this.identity = identity;
        this.redirectId = redirectId;
    }

    /**
     * Increments the current size of the quorum and checks if it is complete. <br>
     * When complete, sends the adequate reply (either client or other node).
     * @return True if the quorum is complete, false otherwise.
     */
    public synchronized boolean increment() {
        this.currentSize++;
        if (isQuorum()) {
            finish();
            return true;
        }
        return false;
    }

    public String getId() {
        return id;
    }

    public boolean isQuorum() {
        return this.currentSize >= this.quorumSize;
    }

    public void addList(ShoppingList list) {
        // TODO: merge lists (crdts)
        this.list = list;
    }

    /**
     * Finish up the quorum. Reply to the client or to other node if the request
     * came from a redirect.
     */
    private void finish() {
        if (this.list == null) finishWrite();
        else finishRead();
    }

    private void finishWrite() {
        if (this.nodeSocket != null) {
            new NodeConnector(this.nodeSocket).sendRedirectWriteReply(this.redirectId);
        } else {
            ZMQ.Socket socket =  Store.getInstance().getClientBroker();

            socket.send(identity, ZMQ.SNDMORE);
            socket.send("".getBytes(), ZMQ.SNDMORE);
            socket.send("", 0);
        }
    }

    private void finishRead() {
        if (this.nodeSocket != null) {
            new NodeConnector(this.nodeSocket).sendRedirectReadReply(this.list, this.redirectId);
        } else {
            ZMQ.Socket socket =  Store.getInstance().getClientBroker();

            String listJSON = new com.google.gson.Gson().toJson(this.list);
            socket.send(identity, ZMQ.SNDMORE);
            socket.send("".getBytes(), ZMQ.SNDMORE);
            socket.send(listJSON, 0);
        }
    }
}
