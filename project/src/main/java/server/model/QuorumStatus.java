package server.model;

import org.zeromq.ZMQ;
import server.Store;
import server.connections.NodeConnector;

public class QuorumStatus {
    private int quorumSize;
    private int currentSize;
    private String id;
    private ZMQ.Socket nodeSocket; // for redirect
    private byte[] identity; // for client responses
    private String redirectId;

    public QuorumStatus(int quorumSize, ZMQ.Socket nodeSocket, byte[] identity, String redirectId) {
        this.quorumSize = quorumSize;
        this.currentSize = 0;
        this.id = java.util.UUID.randomUUID().toString();
        this.nodeSocket = nodeSocket;
        this.identity = identity;
        this.redirectId = redirectId;
    }

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

    private void finish() {
        if (this.nodeSocket != null) {
            new NodeConnector(this.nodeSocket).sendRedirectWriteReply(this.redirectId);
        } else {
            replyClient("");
        }
    }

    private void replyClient(String response) {
        ZMQ.Socket socket =  Store.getInstance().getClientBroker();

        socket.send(identity, ZMQ.SNDMORE);
        socket.send("".getBytes(), ZMQ.SNDMORE);
        socket.send(response, 0);
    }
}
