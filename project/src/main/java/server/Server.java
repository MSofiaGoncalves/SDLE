package server;

import server.db.Database;
import server.model.ShoppingList;

public class Server
{
    public static void main(String[] args) throws Exception
    {
        Database db = Database.getInstance();
        System.out.println(db.insertList(new ShoppingList("123", "HELLO")));
        System.out.println(db.insertList(new ShoppingList("123", "test")));
        ShoppingList l = db.getList("123");
        System.out.println(l.getName());
        db.removeList("123");

        return;
        /*
        try (ZContext context = new ZContext()) {
            // Socket to talk to clients
            ZMQ.Socket socket = context.createSocket(ZMQ.REP);
            socket.bind("tcp://*:5555");

            while (!Thread.currentThread().isInterrupted()) {
                System.out.println("Inside the while loop");
                // Block until a message is received
                byte[] reply = socket.recv(0);

                // Print the message
                System.out.println(
                        "Received: [" + new String(reply, ZMQ.CHARSET) + "]"
                );

                // Send a response
                String response = "Hello, world!";
                socket.send(response.getBytes(ZMQ.CHARSET), 0);

            }
            System.out.println("While loop exited");
        }
        System.out.println("Server ended");
        */
    }
}