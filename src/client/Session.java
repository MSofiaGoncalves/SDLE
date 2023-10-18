package client;

import model.ShoppingList;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class Session {
    private HashMap<String, ShoppingList> lists;
    private static Session instance;

    private Session() {
        lists = new HashMap<>();
    }

    /**
     * Creates a list.
     * Saves it to local storage and sends it to the server.
     * @return Newly created list object.
     */
    public ShoppingList createList(String name) {
        // TODO send to server & check id
        String id = UUID.randomUUID().toString();
        ShoppingList shoppingList = new ShoppingList(id, name);
        this.lists.put(shoppingList.getId(), shoppingList);
        return shoppingList;
    }

    public ShoppingList getList(String id) {
        return this.lists.get(id);
    }

    public List<ShoppingList> getLists() {
        return new ArrayList<>(this.lists.values());
    }

    public static synchronized Session getSession() {
        if (instance == null) {
            instance = new Session();
        }
        return instance;
    }
}
