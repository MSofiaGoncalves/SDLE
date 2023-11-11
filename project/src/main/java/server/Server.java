package server;

import org.zeromq.ZMQ;

import java.util.logging.Logger;

public class Server {
    public static void main(String[] args) throws Exception {
        parseArgs(args);

        Store store = Store.getInstance();
        store.initConnections();
        ZMQ.Socket socket = store.getClientBroker();
        Logger logger = Store.getLogger();
        logger.info("Sever listening on port 5555.");

        while (!Thread.currentThread().isInterrupted()) {
            // Block until a message is received
            byte[] clientIdentity = socket.recv();

            // Receive empty delimiter frame
            socket.recv();

            String request = socket.recvStr();

            MessageHandler messageHandler = new MessageHandler(clientIdentity, request);
            store.execute(messageHandler);
        }
    }

    private static void parseArgs(String[] args) {
        Store store = Store.getInstance();
        if (args.length == 0) {
            throw new IllegalArgumentException("Usage: server <clienthost> <nodehost>");
        }
        store.setProperty("clienthost", args[0]);
        if (args.length >= 2) {
            store.setProperty("nodehost", args[1]);
        }
    }
}