package server;

import org.zeromq.ZMQ;
import server.threads.ClientHandler;
import server.threads.FailureDetector;
import server.threads.NodeHandler;

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
        Store.initLogger();
        if (args.length == 0) {
            Store.logger.info("Usage: server <clienthost> <nodehost>");
            Store.logger.info("Warning: No hostname specified, using defaults.");
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
}