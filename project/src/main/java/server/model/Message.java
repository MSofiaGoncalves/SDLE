package server.model;

import com.google.gson.Gson;

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