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
    private String listId;
    private ShoppingList list;
    private String address; // use to identify clients
    private String quorumId;
    private String clientIdentity;

    public Message(String message) {
        new Gson().fromJson(message, Message.class);
    }

    public String getMethod() {
        return method;
    }

    public String getListId() {
        return listId;
    }

    public ShoppingList getList() {
        return list;
    }

    public String getQuorumId() {
        return quorumId;
    }

    public String getClientIdentity() {
        return clientIdentity;
    }

    public String getAddress() {
        return address;
    }
}