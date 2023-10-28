package client.model;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import client.utils.ProductQuantity;
import client.utils.TablePrinter;
import com.google.gson.*;
import zmq.socket.Pair;

public class ShoppingList {
    private String id;
    private String name;
    private Map<String, ProductQuantity> products;

    public ShoppingList(String id, String name) {
        this.id = id;
        this.name = name;
        this.products = new HashMap<>();
    }

    public ShoppingList(String id, String name, Map<String, ProductQuantity> products) {
        this.id = id;
        this.name = name;
        this.products = products;
    }

    public String getId() {
        return this.id;
    }

    public String getName() {
        return this.name;
    }

    public Map<String, ProductQuantity> getProducts() {
        return products;
    }

    public void addProduct(String name, int quantity) {
        if (products.containsKey(name)) {
            addProductQuantity(name, quantity);
        } else {
            ProductQuantity quantities = new ProductQuantity(quantity, 0);
            products.put(name, quantities);
        }
    }

    public void removeProduct(String name, int quantity) {
        products.remove(name);
    }

    public void addProductQuantity(String name, int quantity) {
        ProductQuantity currQuantities = products.get(name);
        currQuantities.addToList(quantity);
    }

    public void buyProductQuantity(String name, int quantity) {
        ProductQuantity currQuantities = products.get(name);
        currQuantities.buyQuantity(quantity);
    }

    public void printProducts() {
        List<List<String>> data = new ArrayList<>();
        data.add(List.of("Product Name", "Quantity", "Quantity Bought"));

        for (Map.Entry<String, ProductQuantity> product : products.entrySet()) {
            String productName = product.getKey();
            ProductQuantity quantities = product.getValue();
            data.add(List.of(productName, Integer.toString(quantities.getQuantity()), Integer.toString(quantities.getQuantityBought())));
        }

        TablePrinter.printTable(data);
    }

    public Boolean hasProducts() {
        return this.products.size() != 0;
    }

    public void saveToFile() {
        Gson gson = new Gson();
        String json = gson.toJson(this);
        String fileName = "src/main/java/client/lists/" + this.id + ".json";
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(fileName))) {
            writer.write(json);
        } catch (IOException e) {
            System.out.println("Error saving to file.");
        }
    }

    public static ShoppingList loadFromFile(String fileName) {
        try(Reader reader = new FileReader(fileName)){
            Gson gson = new Gson();
            return gson.fromJson(reader, ShoppingList.class);
        } catch (IOException e) {
            System.out.println("Error loading from file.");
            return null;
        }
    }
}
