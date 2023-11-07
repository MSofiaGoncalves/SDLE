package client;

import client.model.ShoppingList;
import java.util.Scanner;
import org.zeromq.ZContext;
import org.zeromq.ZMQ;

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

    public Session(String username) {

        lists = loadListsFromFiles();
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
        ServerConnector connector = Session.getConnector();
        ShoppingList shoppingList = new ShoppingList(name);
        connector.insertList(shoppingList);
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
        ShoppingList shoppingList = connector.getList(id);
        if (shoppingList != null) {
            this.lists.put(shoppingList.getId(), shoppingList);
        }
        return this.lists.get(id);
    }

    //store individually considering the username
    public HashMap<String, ShoppingList> loadListsFromFiles() {
        HashMap<String, ShoppingList> shoppingLists = new HashMap<>();
        File folder = new File("src/main/java/client/lists/" + username);
        System.out.println(folder.getAbsolutePath());
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

    public static String getUsername() {
        return username;
    }

    public static String setUsername(String name){
        username = name;
        return username;
    }

    public static synchronized Session getSession() {
        if (instance == null) {

            System.out.println("Username no getSession: " + username);

            instance = new Session(username);
        }
        return instance;
    }
}
