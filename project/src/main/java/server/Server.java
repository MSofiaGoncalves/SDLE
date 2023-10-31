package server;

import org.zeromq.ZContext;
import org.zeromq.ZMQ;
import server.db.Database;
import server.model.ShoppingList;
import server.utils.Pair;

public class Server {
    public static void main(String[] args) throws Exception {
        ZMQ.Socket socket = Store.getSocket();
        System.out.println("Sever listening on port 5555.");
        while (!Thread.currentThread().isInterrupted()) {
            // Block until a message is received
            byte[] clientIdentity = socket.recv();

            // Receive empty delimiter frame
            socket.recv();

            String request = socket.recvStr();

            MessageHandler messageHandler = new MessageHandler(clientIdentity, request);
            Store.execute(messageHandler);
    }
        System.out.println("Server ended");
}
}