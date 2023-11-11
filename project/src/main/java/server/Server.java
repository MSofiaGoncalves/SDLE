package server;

import org.zeromq.ZMQ;

import java.util.logging.Logger;

public class Server {
    public static void main(String[] args) throws Exception {
        ZMQ.Socket socket = Store.getSocket();
        Logger logger = Store.getLogger();
        logger.info("Sever listening on port 5555.");

        while (!Thread.currentThread().isInterrupted()) {
            // Block until a message is received
            byte[] clientIdentity = socket.recv();

            // Receive empty delimiter frame
            socket.recv();

            String request = socket.recvStr();

            MessageHandler messageHandler = new MessageHandler(clientIdentity, request);
            Store.execute(messageHandler);
        }
    }




}