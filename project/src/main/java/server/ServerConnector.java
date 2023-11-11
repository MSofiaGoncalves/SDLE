package server;

import org.zeromq.ZMQ;

import java.util.HashMap;
import java.util.Map;

public class ServerConnector {
    private static ServerConnector instance = null;
    private Map<String, ZMQ.Socket> nodes;

    public ServerConnector() {
        nodes = new HashMap<String, ZMQ.Socket>();

        Store store = Store.getInstance();
        for (String nodeUrl: store.getProperty("nodes").split(";")) {
            ZMQ.Socket socket = store.getContext().createSocket(ZMQ.DEALER);
            socket.connect(nodeUrl);
            nodes.put(nodeUrl, socket);
        }
    }

    public static ServerConnector getInstance() {
        if (instance == null) {
            instance = new ServerConnector();
        }
        return instance;
    }
}
