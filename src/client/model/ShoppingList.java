package model;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.io.FileWriter;
import java.io.IOException;

public class ShoppingList {
    private String id;
    private String name;
    private Map<String, Integer> products;

    public ShoppingList(String id, String name) {
        this.id = id;
        this.name = name;
        this.products = new HashMap<>();
    }

    public String getId() {
        return this.id;
    }

    public String getName() {
        return this.name;
    }

    public Map<String, Integer> getProducts() {
        return products;
    }

    public void addProduct(String name, int quantity) {
        if (products.containsKey(name)) {
            System.out.println("exists");
            addQuantity(name, quantity);
        } else {
            products.put(name, quantity);
        }
    }

    public void removeProduct(String name, int quantity) {
        products.remove(name);
    }

    public void addQuantity(String name, int quantity) {
        Integer currQuantity = products.get(name);
        System.out.println("Cur quantity: " + currQuantity);
        System.out.println("New quantity: " + (currQuantity + quantity));
        products.put(name, currQuantity + quantity);
    }

    public void removeQuantity(String name, int quantity) {
        Integer currQuantity = products.get(name);
        if (currQuantity - quantity > 1) {
            products.put(name, currQuantity - quantity);
        } else {
            products.remove(name);
        }
    }

    public void printProducts() {
        System.out.println("\t* Product: Quantity ");
        for (Map.Entry<String, Integer> entry : products.entrySet()) {
            System.out.println("\t" + entry.getKey() + ": " + entry.getValue());
        }
        System.out.println("\n");
    }

    public Boolean hasProducts() {
        return this.products.size() != 0;
    }

    public JSONObject toJSonObject() {
        JSONObject shoppingListJson = new JSONObject();
        shoppingListJson.put("name", name);
        shoppingListJson.put("id", id);

        JSONArray productsArray = new JSONArray();
        for (Map.Entry<String, Integer> entry : products.entrySet()) {
            JSONObject productJson = new JSONObject();
            productJson.put("name", entry.getKey());
            productJson.put("quantity", entry.getValue());
            productsArray.add(productJson);
        }

        shoppingListJson.put("products", productsArray);
        return shoppingListJson;
    }

    public void saveToFile() {
        String fileName = "client/lists/" + id + ".json";
        JSONObject jsonObject = toJSonObject();

        try (FileWriter file = new FileWriter(fileName)) {
            file.write(jsonObject.toJSONString());
            System.out.println("Shopping list has been written to " + fileName);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // TODO: Function that reads from json file
}
