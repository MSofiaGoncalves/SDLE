package client;

import client.states.HubState;
import client.states.LoginState;
import client.states.State;

public class Client {
    public static void main(String[] args) {
        State state = new LoginState();

        while (state != null) {
            state = state.step();
        }
    }
}
/*
import org.zeromq.ZMQ;
import org.zeromq.ZContext;

public class Client
{
    public static void main(String[] args)
    {
        try (ZContext context = new ZContext()) {
            //  Socket to talk to server
            System.out.println("Connecting to hello world server");

            ZMQ.Socket socket = context.createSocket(ZMQ.REQ);
            socket.connect("tcp://localhost:5555");

            for (int requestNbr = 0; requestNbr != 10; requestNbr++) {
                String request = "Hello";
                System.out.println("Sending Hello " + requestNbr);
                socket.send(request.getBytes(ZMQ.CHARSET), 0);

                byte[] reply = socket.recv(0);
                System.out.println(
                        "Received " + new String(reply, ZMQ.CHARSET) + " " +
                                requestNbr
                );
            }
        }
    }
}*/