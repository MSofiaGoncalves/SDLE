package server;

import server.db.Database;
import server.model.ShoppingList;
import com.google.gson.Gson;
import org.zeromq.ZMQ;
import server.model.Message;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public class MessageHandler implements Runnable {
    private byte[] identity;
    private String messageRaw;
    private Message message;

    public MessageHandler(byte[] identity, String message) {
        this.identity = identity;
        this.messageRaw = message;
    }

    @Override
    public void run() {
        message = new Gson().fromJson(messageRaw, Message.class);

        Map<String, Function<Void, Void>> functionMap = new HashMap<>();
        functionMap.put("insert", this::insertList);
        functionMap.put("get", this::getList);

        functionMap.get(message.getMethod()).apply(null);
    }

    private Void insertList(Void unused) {
        System.out.println("Inserting list, " + message.getList().getId() + ", into database.");
        Database.getInstance().insertList(message.getList());

        reply("");
        return null;
    }

    private Void getList(Void unused) {
        ShoppingList list = Database.getInstance().getList(message.getId());
        reply(new Gson().toJson(list));
        return null;
    }

    private void reply(String response) {
        ZMQ.Socket socket =  Store.getSocket();

        socket.send(identity, ZMQ.SNDMORE);
        socket.send("".getBytes(), ZMQ.SNDMORE);
        socket.send(response, 0);
    }
}
