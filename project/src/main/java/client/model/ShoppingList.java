package client.model;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

import client.ServerConnector;
import client.Session;
import client.states.AddProductState;
import client.utils.TablePrinter;
import com.google.gson.*;
import crdts.AddWins;
import crdts.PNCounter;
import crdts.utils.Triple;

import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousFileChannel;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.concurrent.Future;
import java.util.concurrent.CompletableFuture;

public class ShoppingList {
    private String id;
    private String name;

    private Map<String, Product> products;

    private AddWins addWins;

    public boolean deleted;

    public ShoppingList(String name) {
        this.id = java.util.UUID.randomUUID().toString();
        this.name = name;
        this.products = new HashMap<>();
        this.addWins = new AddWins(this.id);
        this.deleted = false;
        save();
    }

    public ShoppingList(String id, String name) {
        this.id = id;
        this.name = name;
        this.products = new HashMap<>();
        this.addWins = new AddWins(this.id);
        this.deleted = false;
        saveToFile();
    }

    public void setDeleted(boolean deleted){
        this.deleted = deleted;
    }

    public boolean getDeleted(){
        return deleted;
    }

    public String getId() {
        return this.id;
    }

    public String getName() {
        return this.name;
    }

    public Map<String, Product> getProducts() {
        return products;
    }

    public void addProduct(String name, int quantity) {
        if(quantity < 0){
            System.out.println("Invalid quantity.");
            return;
        }
        if (products.containsKey(name)) {
            addProductQuantity(name, quantity);
        }
        else {
            Product p = new Product(name, quantity);
            products.put(p.getName(), p);
            addWins.add(p.getName(), Session.getSession().getUsername());
        }
        save();
    }

    // add or remove quantity
    public void addProductQuantity(String name, int quantity) {
        if(quantity < 0){
            System.out.println("Invalid quantity.");
            return;
        }
        if(productExists(name)){
            products.get(name).addQuantity(quantity);
        }
        else{
            System.out.println("Invalid product.");
        }
        save();
    }

    public void removeProductQuantity(String name, int quantity) {
        if(quantity < 0){
            System.out.println("Invalid quantity.");
            return;
        }
        if(productExists(name)){
            if(quantity >= products.get(name).getQuantity()){
                deleteProduct(name);
            }
            else {
                products.get(name).removeQuantity(quantity);
            }
        }
        else{
            System.out.println("Invalid product.");
        }
        save();
    }

    public boolean productExists(String name){
        return products.containsKey(name);
    }

    public void buyProductQuantity(String name, int quantity) {
        if(productExists(name)) {
            products.get(name).buyQuantity(quantity);
        }
        else{
            System.out.println("Invalid product.");
        }
        save();
    }

    public void printProducts() {
        List<List<String>> data = new ArrayList<>();
        data.add(List.of("Product Name", "Quantity", "Quantity Bought"));

        for (Map.Entry<String, Product> product : products.entrySet()) {
            String productName = product.getKey();
            data.add(List.of(productName, Integer.toString(product.getValue().getQuantity()), Integer.toString(product.getValue().getQuantityBought())));
        }

        TablePrinter.printTable(data);
    }

    public Boolean hasProducts() {
        return !this.products.isEmpty();
    }


    public Boolean hasProduct(String name){
        return this.products.containsKey(name);
    }

    public void deleteProduct(String name){
        this.products.remove(name);
        this.addWins.rm(name);
        save();
    }

    public static ShoppingList loadFromFile(String fileName) {
        try (Reader reader = new FileReader(fileName)) {
            Gson gson = new Gson();
            return gson.fromJson(reader, ShoppingList.class);
        } catch (Exception e) {
            System.out.println("Error loading from file: " + e.getMessage());
            return null;
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof ShoppingList list)) {
            return false;
        }
        return this.id.equals(list.getId()) && this.name.equals(list.getName()) && this.products.equals(list.getProducts());
    }

    private void saveToFile() {
        String username = Session.getSession().getUsername();
        Gson gson = new Gson();
        String json = gson.toJson(this);
        String directoryPath = "src/main/java/client/lists/" + username + "/";
        String fileName = directoryPath + this.id + ".json";


        try {
            Path path = Paths.get(fileName);
            File directory = path.getParent().toFile();

            if (!directory.exists()) {
                directory.mkdirs();
            }

            AsynchronousFileChannel fileChannel = AsynchronousFileChannel.open(path, StandardOpenOption.WRITE, StandardOpenOption.CREATE,
                    StandardOpenOption.TRUNCATE_EXISTING);

            ByteBuffer buffer = ByteBuffer.wrap(json.getBytes());

            // Asynchronously write to the file
            Future<Integer> writeFuture = fileChannel.write(buffer, 0);

            // Optionally, you can wait for the write operation to complete
            writeFuture.get();

            // Close the file channel
            fileChannel.close();
        } catch (Exception e) {
            System.out.println("Error saving to file asynchronously: " + e.getMessage());
        }
    }


    public AddWins getAddWins() {
        return addWins;
    }

    public void deleteList(){
        this.deleted = true;
        this.save();
    }

    public void mergeListsClient(ShoppingList list){
        this.addWins.join(list.getAddWins());
        Set<String> keySet = this.products.keySet();
        for(String key : keySet){
            if(!this.addWins.containsProduct(key)) {
                this.deleteProduct(key);
            }
        }
        for(Triple<String, String, Long> triple : this.addWins.getSet()){
            if(!this.products.containsKey(triple.getSecond())){
                Product p = new Product(triple.getSecond(), 0);
                this.products.put(p.getName(), p);
            }
        }

        for (Map.Entry<String, Product> product : products.entrySet()) {
            if(this.getProducts().containsKey(product.getKey()) && list.getProducts().containsKey(product.getKey())){
                product.getValue().mergeProduct(list.getProducts().get(product.getKey()));
            }
        }
        if(this.deleted || list.getDeleted()){
            this.deleted = true;
        }
    }

    /**
     * Saves the list to the server and local storage. <br>
     * Should be called everytime the list is modified.
     */
    public void save() {
        Session.getSession().addShoppingList(this);
        Session.getConnector().writeList(this);
        saveToFile();
    }

}
