package server;

import org.zeromq.ZMQ;
import server.threads.ClientHandler;
import server.threads.NodeHandler;

public class Server {

    public static void main(String[] args) throws Exception {
        parseArgs(args);

        Store store = Store.getInstance();
        store.initConnections();
        checkParams();
        ZMQ.Socket clientBroker = store.getClientBroker();

        ZMQ.Poller poller = store.getContext().createPoller(1);
        poller.register(clientBroker, ZMQ.Poller.POLLIN);
        for (ZMQ.Socket node: store.getNodes().values()) {
            poller.register(node, ZMQ.Poller.POLLIN);
        }

        while (!Thread.currentThread().isInterrupted()) {
            poller.poll();

            if (poller.pollin(0)) { // Client message
                byte[] clientIdentity = clientBroker.recv();

                // Receive empty delimiter frame
                clientBroker.recv();

                String request = clientBroker.recvStr();

                ClientHandler clientHandler = new ClientHandler(clientIdentity, request);
                store.execute(clientHandler);
            } else { // Node message
                for (int i = 1; i < poller.getSize(); i++) {
                    if (poller.pollin(i)) {
                        ZMQ.Socket node = poller.getSocket(i);
                        byte[] msg = node.recv();
                        NodeHandler nodeHandler = new NodeHandler(node, new String(msg, ZMQ.CHARSET));
                        store.execute(nodeHandler);
                    }
                }
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
}