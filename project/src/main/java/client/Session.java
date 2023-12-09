package client;

import client.model.ShoppingList;

import java.io.File;
import java.io.FileInputStream;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Singleton class that holds the current session.
 */
public class Session {
    private ConcurrentHashMap<String, ShoppingList> lists;
    private static Session instance;
    private static ServerConnector connector;
    public static String username;
    private Properties properties;
    ScheduledExecutorService refresher;

    public Session() {
        initProperties();
        lists = new ConcurrentHashMap<>();
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
        try {
            refresher.awaitTermination(2 * Long.parseLong(getProperty("refreshTime")), TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
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

    public ShoppingList getLocalList(String id) {
        return this.lists.get(id);
    }

    /**
     * Gets a list from the server or from local storage. <br>
     * This method is asynchronous. Does not return the list but updates the map.
     * @param id Id of the list to get.
     */
    public void startRefresher(String id) {
        stopRefresher();
        refresher = Executors.newScheduledThreadPool(1);
        Runnable runnable = () -> {
                ServerConnector connector = Session.getConnector();
                ShoppingList shoppingList = connector.readList(id);
                if (shoppingList != null) {
                    this.lists.put(shoppingList.getId(), shoppingList);
                }
        };
        refresher.scheduleAtFixedRate(runnable, 0, Long.parseLong(getProperty("refreshTime")), TimeUnit.MILLISECONDS);
    }

    public void stopRefresher() {
        if (refresher == null) return;
        refresher.shutdown();
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
        this.lists = new ConcurrentHashMap<>(loadListsFromFiles());
    }

    public static synchronized Session getSession() {
        if (instance == null) {
            instance = new Session();
        }
        return instance;
    }

    public String getProperty(String key) {
        return properties.getProperty(key);
    }

    private void initProperties() {
        if (properties == null)
            properties = new Properties();
        final String filePath = "src/main/java/client/client.properties";
        try {
            properties.load(new FileInputStream(filePath));
        } catch (Exception e) {
            throw new RuntimeException("Unable to load properties file: " + filePath, e);
        }
        initHosts();
    }

    public void setProperty(String key, String value) {
        properties.setProperty(key, value);
    }

    private void initHosts() {
        if (getProperty("serverhost") == null) {
            setProperty("serverhost", getProperty("serverhostdefault"));
        }

        String[] temp = getProperty("serverhost").split(":");
        setProperty("serverPort", temp[temp.length - 1]);
    }
}
