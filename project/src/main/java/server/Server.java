package server;

import org.zeromq.ZMQ;
import server.connections.NodeConnector;
import server.threads.ClientHandler;
import server.threads.FailureDetector;
import server.threads.NodeHandler;

import java.time.Instant;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class Server {

    public static void main(String[] args) throws Exception {
        parseArgs(args);

        Store store = Store.getInstance();
        store.initConnections();
        checkParams();
        ZMQ.Socket clientBroker = store.getClientBroker();
        ZMQ.Socket nodeBroker = store.getNodeBroker();

        ZMQ.Poller poller = store.getContext().createPoller(1);
        poller.register(clientBroker, ZMQ.Poller.POLLIN);
        poller.register(nodeBroker, ZMQ.Poller.POLLIN);

        startFailureDetector();
        sendHeartbeats();

        while (!Thread.currentThread().isInterrupted()) {
            poller.poll();

            if (poller.pollin(0)) { // Client message
                byte[] clientIdentity = clientBroker.recv();

                // Receive empty delimiter frame
                clientBroker.recv();

                String request = clientBroker.recvStr();

                ClientHandler clientHandler = new ClientHandler(clientIdentity, request);
                store.execute(clientHandler);
            }
            if (poller.pollin(1)) { // Node message
                byte[] identity = nodeBroker.recv();
                byte[] msg = nodeBroker.recv();
                NodeHandler nodeHandler = new NodeHandler(new String(identity, ZMQ.CHARSET), new String(msg, ZMQ.CHARSET));
                store.execute(nodeHandler);
            }
        }
    }

    private static void parseArgs(String[] args) {
        Store store = Store.getInstance();
        if (args.length == 0) {
            System.out.println("Usage: server <clienthost> <nodehost>");
            System.out.println("Warning: No hostname specified, using defaults.");
            return;
        }
        store.setProperty("clienthost", args[0]);
        if (args.length >= 2) {
            store.setProperty("nodehost", args[1]);
        }
    }

    /**
     * Checks if the parameters are valid. <br>
     */
    private static void checkParams() {
        Store store = Store.getInstance();
        int quorumN = Integer.parseInt(Store.getProperty("quorumNumber"));
        int quorumW = Integer.parseInt(Store.getProperty("quorumWrites"));
        int quorumR = Integer.parseInt(Store.getProperty("quorumReads"));
        if (quorumN < quorumW) {
            Store.logger.warning("Quorum number is less than quorum writes. Setting quorum number to quorum writes.");
            store.setProperty("quorumNumber", store.getProperty("quorumWrites"));
            quorumN = quorumW;
        }
        if (quorumN < quorumR) {
            Store.logger.warning("Quorum number is less than quorum reads. Setting quorum number to quorum reads.");
            store.setProperty("quorumNumber", store.getProperty("quorumReads"));
        }
    }

    private static void startFailureDetector() {
        FailureDetector failureDetector = new FailureDetector();
        ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
        scheduler.scheduleAtFixedRate(failureDetector, 0, Integer.parseInt(Store.getProperty("failureTimeout")), TimeUnit.MILLISECONDS);
    }

    /**
     * When starting, the node will try to connect to all other nodes during 10 seconds.
     */
    private static void sendHeartbeats() {
        Thread thread = new Thread(() -> {
            Instant start = Instant.now();
            try {
                while (Instant.now().isBefore(start.plusMillis(10000))) {
                    Thread.sleep(1000);
                    for (String node : Store.getInstance().getNodes()) {
                        if (node.equals(Store.getProperty("nodehost"))) continue;
                        if (Store.getInstance().getHashRing().getNodeStatus(node)) continue;
                        Store.getInstance().getNodeBroker().connect(node);
                        new NodeConnector(node).sendHeartbeat();
                    }
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });
        thread.start();
    }
}