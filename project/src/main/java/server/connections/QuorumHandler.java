package server.connections;

import org.zeromq.ZMQ;
import server.Store;
import server.db.Database;
import server.model.ShoppingList;

enum QuorumOperation {
    READ,
    WRITE
}

public class QuorumHandler implements Runnable {
    private byte[] clientIdentity;
    private String listId;
    private QuorumOperation operation;
    private ShoppingList list;

    public QuorumHandler(String listId, QuorumOperation operation) {
        if (operation == QuorumOperation.WRITE) {
            throw new IllegalArgumentException("Cannot write a list with only the id.");
        }
        this.listId = listId;
        this.operation = operation;
    }

    public QuorumHandler(ShoppingList list, QuorumOperation operation) {
        if (operation == QuorumOperation.READ) {
            throw new IllegalArgumentException("Cannot write a list with only the id.");
        }
        this.list = list;
        this.operation = operation;
        this.listId = list.getId();
    }

    public void setClientIdentity(byte[] clientIdentity) {
        this.clientIdentity = clientIdentity;
    }

    @Override
    public void run() {
        if (operation == QuorumOperation.READ) {
            read();
        } else {
            write();
        }
    }

    private void read() {
        // TODO: reading
        String[] nodes = Store.getInstance().getHashRing().getNodes(listId);
        int r = Integer.parseInt(Store.getProperty("quorumReads"));

    }

    private void write() {
        int nWritten = 0;

        Store.getLogger().info("Writing list: " + list.getId() + " into database.");
        Database.getInstance().insertList(list);
        nWritten++;

        Store store = Store.getInstance();
        String[] nodes = store.getHashRing().getNodes(listId);

        int r = Integer.parseInt(Store.getProperty("quorumWrites"));
        ZMQ.Poller poller = store.getContext().createPoller(nodes.length);
        for (String node : nodes) {
            if (node.equals(Store.getProperty("nodehost"))) {
                continue;
            }
            ZMQ.Socket socket = new NodeConnector(node).sendListWrite(list);
            poller.register(socket, ZMQ.Poller.POLLIN);
        }

        while (nWritten < r) {
            poller.poll();
            for (int i = 0; i < poller.getSize(); i++) {
                if (poller.pollin(i)) {
                    nWritten++;
                }
            }
        }
        // TODO: detect failures & delay max

        if (clientIdentity != null) {
            reply("");
        }
    }

    private void reply(String response) {
        ZMQ.Socket socket =  Store.getInstance().getClientBroker();

        socket.send(clientIdentity, ZMQ.SNDMORE);
        socket.send("".getBytes(), ZMQ.SNDMORE);
        socket.send(response, 0);
    }
}
