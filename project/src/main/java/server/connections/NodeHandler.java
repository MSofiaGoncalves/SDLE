package server.connections;

import com.google.gson.Gson;
import org.zeromq.ZMQ;
import server.Store;
import server.db.Database;
import server.model.Message;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * Handles messages from the nodes.
 */
public class NodeHandler implements Runnable {
    private ZMQ.Socket socket;
    private String messageRaw;
    private Message message;

    /**
     * Creates a new NodeHandler.
     * @param socket The socket to send replies to.
     * @param message The message to handle.
     */
    public NodeHandler(ZMQ.Socket socket, String message) {
        this.messageRaw = message;
        this.message = new Gson().fromJson(messageRaw, Message.class);
    }

    @Override
    public void run() {
        Store.getLogger().info("Received list: " + message.getList().getId());

        Map<String, Function<Void, Void>> functionMap = new HashMap<>();
        functionMap.put("insert", this::insertList);

        functionMap.get(message.getMethod()).apply(null);
    }

    private Void insertList(Void unused) {
        Store.getLogger().info("Inserting list, " + message.getList().getId() + ", into database.");
        Database.getInstance().insertList(message.getList());
        return null;
    }
}
