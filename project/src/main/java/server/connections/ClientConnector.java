package server.connections;

import org.zeromq.ZMQ;
import server.Store;

public class ClientConnector {
    public static void clientReply(byte[] clientIdentity, String reply) {
        ZMQ.Socket clientBroker =  Store.getInstance().getClientBroker();
        clientBroker.send(clientIdentity, ZMQ.SNDMORE);
        clientBroker.send("".getBytes(), ZMQ.SNDMORE);
        clientBroker.send(reply.getBytes(), 0);
    }
}
