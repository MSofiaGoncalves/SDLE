package server;

import com.mongodb.internal.operation.SyncOperations;
import org.zeromq.ZMQ;

import java.util.logging.Logger;
import java.util.Properties;
import java.io.InputStream;
import server.ConfigLoader;

public class Server {
    public static void main(String[] args) throws Exception {

        ConfigLoader.initializeServer();

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