package client;

import client.model.ShoppingList;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class Session {
    private HashMap<String, ShoppingList> lists;
    private static Session instance;

    private Session() {
        System.out.println("constructor");
        lists = new HashMap<>();
//        lists = loadListsFromFiles();
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

//    public HashMap<String, ShoppingList> loadListsFromFiles(){
//        HashMap<String, ShoppingList> shoppingLists = new HashMap<>();
//        File folder = new File("src/main/java/project/client/lists");
//        System.out.println("folder: " + folder);
//
//        if (folder.exists() && folder.isDirectory()) {
//            System.out.println("exists");
//
//            File[] files = folder.listFiles((dir, name) -> name.toLowerCase().endsWith(".json"));
//            System.out.println("files: " + files);
//
//
//            if (files != null) {
//                System.out.println("nt null");
//                for (File file : files) {
//                    ShoppingList list = ShoppingList.loadFromFile(file.getAbsolutePath());
//                    System.out.println("AAAAAAAAA");
//                    System.out.println("list id: " + list.getId());
//
//                    if (list != null) {
//                        shoppingLists.put(list.getId(), list);
//                    }
//                }
//            }
//            else{
//                System.out.println("no files!");
//            }
//        }
//        return shoppingLists;
//    }

    public List<ShoppingList> getLists() {
        System.out.println("getlists");
        return new ArrayList<>(this.lists.values());
    }

    public static synchronized Session getSession() {
        if (instance == null) {
            instance = new Session();
        }
        return instance;
    }
}
