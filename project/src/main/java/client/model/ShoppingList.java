package client.model;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import client.Session;
import client.states.AddProductState;
import client.utils.TablePrinter;
import com.google.gson.*;
import crdts.AddWins;

import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousFileChannel;
import java.nio.file.*;
import java.util.concurrent.Future;

public class ShoppingList {
    private String id;
    private String name;

    //String is the name of the product, Product is the product itself
    private Map<String, Product> products;

    private AddWins addWins;

    public ShoppingList(String name) {
        this.id = java.util.UUID.randomUUID().toString();
        this.name = name;
        this.products = new HashMap<>();
        this.addWins = new AddWins(id);
    }

    public ShoppingList(String id, String name) {
        this.id = id;
        this.name = name;
        this.products = new HashMap<>();
        this.addWins = new AddWins(id);
    }

//    public ShoppingList(String id, String name, Map<String, ProductQuantity> products) {
//        this.id = id;
//        this.name = name;
//        this.products = products;
//    }

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
        if (products.containsKey(name)) {
            addProductQuantity(name, quantity);
        }
        else {
            ProductQuantity quantities = new ProductQuantity(quantity, 0);
            Product p = new Product(name, quantities);
            products.put(p.getName(), p);
            addWins.add(p.getName());
        }
    }

    public void addProductQuantity(String name, int quantity) {
        if(productExists(name)) {
            ProductQuantity currQuantities = products.get(name).getProductQuantity();
            currQuantities.addToList(quantity);
            products.get(name).setProductQuantity(currQuantities);
        }
        else{
            System.out.println("Invalid product.");
        }
    }

    public boolean productExists(String name){
        return products.containsKey(name);
    }

    public void buyProductQuantity(String name, int quantity) {
        if(productExists(name)) {
            ProductQuantity currQuantities = products.get(name).getProductQuantity();
            currQuantities.buyQuantity(quantity);
            products.get(name).setProductQuantity(currQuantities);
        }
        else{
            System.out.println("Invalid product.");
        }
    }

    public void printProducts() {
        List<List<String>> data = new ArrayList<>();
        data.add(List.of("Product Name", "Quantity", "Quantity Bought"));

        for (Map.Entry<String, Product> product : products.entrySet()) {
            String productName = product.getKey();
            ProductQuantity quantities = product.getValue().getProductQuantity();
            data.add(List.of(productName, Integer.toString(quantities.getQuantity()), Integer.toString(quantities.getQuantityBought())));
        }

        TablePrinter.printTable(data);
    }

    public Boolean hasProducts() {
        return !this.products.isEmpty();
    }

    public void saveToFile() {
        String username = Session.getSession().getUsername();
        Gson gson = new Gson();
        String json = gson.toJson(this);
        System.out.println("Json: " + json);
        String directoryPath = "src/main/java/client/lists/" + username + "/";
        String fileName = directoryPath + this.id + ".json";
        System.out.println("Username: " + username + "file: " + fileName);

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


    public Boolean hasProduct(String name){
        return this.products.containsKey(name);
    }

    public void deleteProduct(String name){
        this.products.remove(name);
        addWins.rm(name);
    }

    public static ShoppingList loadFromFile(String fileName) {
        try (Reader reader = new FileReader(fileName)) {
            Gson gson = new Gson();
            return gson.fromJson(reader, ShoppingList.class);
        } catch (IOException e) {
            System.out.println("Error loading from file.");
            return null;
        }
    }


}
