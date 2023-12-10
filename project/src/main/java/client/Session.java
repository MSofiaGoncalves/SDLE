package client;

import client.model.ShoppingList;
import com.google.gson.Gson;

import java.io.File;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousFileChannel;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.*;
import java.util.concurrent.Future;

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

    public void addShoppingList(ShoppingList shoppingList){
        this.lists.put(shoppingList.getId(), shoppingList);
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

    public void deleteList(ShoppingList shoppingList) {
        this.lists.remove(shoppingList);
        //retirar ficheiro json
    }

    /**
     * Gets a list from the server or from local storage.
     * @param id Id of the list to get.
     * @return The list with the given id.
     */
    public ShoppingList getList(String id) {

        ServerConnector connector = Session.getConnector();
        ShoppingList shoppingList = connector.readList(id);
        ShoppingList clientList = isLocalList(id);
        if(clientList != null && shoppingList != null){
            clientList.mergeListsClient(shoppingList);
            return clientList;
        }
        if (shoppingList != null) {
            this.lists.put(shoppingList.getId(), shoppingList);
        }
        return this.lists.get(id);
    }

    /**
     * Checks if a shopping list with the given id exists in the client's folder
     * @param id
     * @return shopping list if it exists and null if it doesn't
     */
    public ShoppingList isLocalList(String id){
        if(lists.containsKey(id)){
            return lists.get(id);
        }
        return null;
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
                    System.out.println("IS DELETED = " + list.getDeleted());
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

        Gson gson = new Gson();
        String json = gson.toJson(this);

        String directoryPath = "src/main/java/client/lists/" + username + "/";

        try {
            File directory = new File(directoryPath);
            if (!directory.exists()) {
                directory.mkdirs();
            }

        } catch (Exception e) {
            System.out.println("Error saving to file asynchronously: " + e.getMessage());
        }
    }

    public static synchronized Session getSession() {
        if (instance == null) {
            instance = new Session();
        }
        return instance;
    }

    public List<ShoppingList> getActiveLists(){
            List<ShoppingList> activeLists = new ArrayList<>();
            for (Map.Entry<String, ShoppingList> entry : lists.entrySet()) {
                if(!entry.getValue().getDeleted()){
                    activeLists.add(entry.getValue());
                }
            }
        return activeLists;
    }
}
