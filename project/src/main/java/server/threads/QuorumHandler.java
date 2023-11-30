package server.threads;

import org.zeromq.ZMQ;
import server.Store;
import server.connections.NodeConnector;
import server.db.Database;
import server.model.QuorumStatus;
import server.model.ShoppingList;

import java.util.Iterator;
import java.util.Set;

enum QuorumMode {
    READ,
    WRITE
}

/**
 * Quorum Leader Handler
 */
public class QuorumHandler implements Runnable {
    private byte[] clientIdentity;
    private ZMQ.Socket nodeSocket;
    private String redirectId;
    private String listId;
    private QuorumMode operation;
    private ShoppingList list;

    public QuorumHandler(String listId, QuorumMode operation) {
        if (operation == QuorumMode.WRITE) {
            throw new IllegalArgumentException("Cannot write a list with only the id.");
        }
        this.listId = listId;
        this.operation = operation;
    }

    /**
     * Creates a new QuorumHandler. Used for WRITE mode.
     * @param list The list to write.
     * @param operation The operation to perform, must be WRITE.
     */
    public QuorumHandler(ShoppingList list, QuorumMode operation) {
        if (operation == QuorumMode.READ) {
            throw new IllegalArgumentException("Cannot write a list with only the id.");
        }
        this.list = list;
        this.operation = operation;
        this.listId = list.getId();
    }

    public void setClientIdentity(byte[] clientIdentity) {
        this.clientIdentity = clientIdentity;
    }

    public void setNodeSocket(ZMQ.Socket nodeSocket) {
        this.nodeSocket = nodeSocket;
    }

    public void setRedirectId(String redirectId) {
        this.redirectId = redirectId;
    }

    @Override
    public void run() {
        if (clientIdentity == null && nodeSocket == null) {
            throw new IllegalStateException("Must set either clientIdentity or nodeSocket.");
        }
        if (operation == QuorumMode.READ) {
            read();
        } else {
            write();
        }
    }

    private void read() {
        // TODO: reading
        Set<String> nodes = Store.getInstance().getHashRing().getNodes(listId);
        int r = Integer.parseInt(Store.getProperty("quorumReads"));

    }

    /**
     * Quorum write leader: <br>
     *    - Create quorum status <br>
     *    - Write to local database <br>
     *    - Inform w - 1 other nodes
     */
    private void write() {
        // Create quorum status
        Store store = Store.getInstance();
        int writeN = Integer.parseInt(Store.getProperty("quorumWrites"));
        QuorumStatus quorumStatus =
                new QuorumStatus(writeN, nodeSocket, clientIdentity, redirectId);
        quorumStatus.increment();
        store.getQuorums().put(quorumStatus.getId(), quorumStatus);

        // Write to local database
        Store.getLogger().info("Writing list: " + list.getId() + " into database.");
        Database.getInstance().insertList(list);

        // Inform w - 1 other nodes
        Set<String> nodes = store.getHashRing().getNodes(listId);
        Iterator<String> iterator = nodes.iterator();
        while (iterator.hasNext()) {
            String node = iterator.next();
            if (node.equals(Store.getProperty("nodehost"))) {
                continue;
            }
            new NodeConnector(node).sendListWrite(list, quorumStatus.getId());
        }
    }
}
