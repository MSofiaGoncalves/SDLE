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
    private String nodeAddress;
    private String redirectId;
    private String listId;
    private QuorumMode operation;
    private ShoppingList list;

    /**
     * Creates a new QuorumHandler. Used for READ mode.
     * @param listId The id list to write.
     * @param operation The operation to perform, must be READ.
     */
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

    public void setNodeAddress(String nodeAddress) {
        this.nodeAddress = nodeAddress;
    }

    public void setRedirectId(String redirectId) {
        this.redirectId = redirectId;
    }

    @Override
    public void run() {
        if (clientIdentity == null && nodeAddress == null) {
            throw new IllegalStateException("Must set either clientIdentity or nodeSocket.");
        }
        if (operation == QuorumMode.READ) {
            read();
        } else {
            write();
        }
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
                new QuorumStatus(writeN, nodeAddress, clientIdentity, redirectId);
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

    private void read() {
        // Create quorum status
        Store store = Store.getInstance();
        int readN = Integer.parseInt(Store.getProperty("quorumReads"));
        QuorumStatus quorumStatus =
                new QuorumStatus(readN, nodeAddress, clientIdentity, redirectId);

        // Read from local database
        Store.getLogger().info("Reading list: " + listId + " from database.");
        list = Database.getInstance().readList(listId);
        if (list == null)
            Store.getLogger().info("List: " + listId + " not found in database.");
        quorumStatus.addList(list);
        if (quorumStatus.increment()) return;

        store.getQuorums().put(quorumStatus.getId(), quorumStatus);

        // Inform w - 1 other nodes
        Set<String> nodes = store.getHashRing().getNodes(listId);
        Iterator<String> iterator = nodes.iterator();
        while (iterator.hasNext()) {
            String node = iterator.next();
            if (node.equals(Store.getProperty("nodehost"))) {
                continue;
            }
            new NodeConnector(node).sendListRead(listId, quorumStatus.getId());
        }
    }
}
