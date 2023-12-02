package client;

import client.model.ShoppingList;

import java.io.File;
import java.util.*;

/**
 * Singleton class that holds the current session.
 */
public class Session {
    private HashMap<String, ShoppingList> lists;
    private static Session instance;
    private static ServerConnector connector;
    public static String username;

    public Session() {
        lists = new HashMap<>();
        connector = new ServerConnector();
    }

    public static ServerConnector getConnector() {
        if (connector == null) {
            connector = new ServerConnector();
        }
        return connector;
    }

    /**
     * Creates a list.
     * Saves it to local storage and sends it to the server.
     *
     * @return Newly created list object.
     */
    public ShoppingList createList(String name) {
        ShoppingList shoppingList = new ShoppingList(name);
        this.lists.put(shoppingList.getId(), shoppingList);
        return shoppingList;
    }

    /**
     * Gets a list from the server or from local storage.
     * @param id Id of the list to get.
     * @return The list with the given id.
     */
    public ShoppingList getList(String id) {
        ServerConnector connector = Session.getConnector();
        ShoppingList shoppingList = connector.readList(id);
        if (shoppingList != null) {
            this.lists.put(shoppingList.getId(), shoppingList);
        }
        return this.lists.get(id);
    }

    /**
     * Load all user's lists from local storage.
     * @return A map of all lists.
     */
    public HashMap<String, ShoppingList> loadListsFromFiles() {
        HashMap<String, ShoppingList> shoppingLists = new HashMap<>();
        File folder = new File("src/main/java/client/lists/" + username);
        if (folder.exists() && folder.isDirectory()) {
            File[] files = folder.listFiles((dir, name) -> name.toLowerCase().endsWith(".json"));
            if (files != null) {
                for (File file : files) {
                    ShoppingList list = ShoppingList.loadFromFile(file.getAbsolutePath());
                    if (list != null) {
                        shoppingLists.put(list.getId(), list);
                    }
                }
            }
        }
        return shoppingLists;
    }

    public List<ShoppingList> getLists() {
        return new ArrayList<>(this.lists.values());
    }

    /**
     * Get the current user's username. <br>
     * This is used only to save the lists to the correct folder.
     * @return String with the username.
     */
    public String getUsername() {
        return username;
    }

    public void setUsername(String name){
        if (instance == null) {
            instance = new Session();
        }
        if (username != null) {
            throw new RuntimeException("Username already set");
        }
        username = name;
        lists = loadListsFromFiles();
    }

    public static synchronized Session getSession() {
        if (instance == null) {
            instance = new Session();
        }
        return instance;
    }
}
