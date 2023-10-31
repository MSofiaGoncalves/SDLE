package server.model;

import com.google.gson.Gson;

/**
 * Represents a message sent from the client to the server.
 *
 * <br>
 * Messages are JSON specified in the following format: <br>
 * <strong>method</strong> - The method to call on the server (e.g. insert) <br>
 * <strong>id</strong> - The id of the list to get (optional) <br>
 * <strong>list</strong> - The list to insert (optional) <br>
 */
public class Message {
    private String method;
    private String id;
    private ShoppingList list;

    public Message(String message) {
        new Gson().fromJson(message, Message.class);
    }

    public String getMethod() {
        return method;
    }

    public String getId() {
        return id;
    }

    public ShoppingList getList() {
        return list;
    }
}