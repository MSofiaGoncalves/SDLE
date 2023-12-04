package server.threads;

import org.zeromq.ZMQ;
import server.Store;
import server.connections.NodeConnector;

import java.time.Instant;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Thread that checks for failed nodes. <br>
 * Runs every <strong>failureTimeout</strong> milliseconds.
 */
public class FailureDetector implements Runnable {
    @Override
    public void run() {
        Store store = Store.getInstance();
        ConcurrentHashMap<String, Instant> waitingReply = store.getWaitingReply();

        for (String address : waitingReply.keySet()) {
            Instant lastReply = waitingReply.get(address);
            if (lastReply.plusMillis(Integer.parseInt(Store.getProperty("failureTimeout"))).isBefore(Instant.now())) {
                Store.getLogger().warning("Node " + address + " is down.");
                waitingReply.remove(address);
                broadcastFailure(address);
            }
        }
    }

    /**
     * Broadcasts a failure to all nodes except the failed node and the current node.
     * @param address The address of the failed node.
     */
    private void broadcastFailure(String address) {
        Store store = Store.getInstance();
        List<String> nodes = store.getNodes();

        for (String node : nodes) {
            if (!node.equals(address) && !node.equals(Store.getProperty("nodehost"))) {
                new NodeConnector(node).sendStatusUpdate(address, false);
            }
        }
    }
}
