package server.threads;

import server.Store;

import java.time.Instant;
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
            }
        }
    }
}
