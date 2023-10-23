package model;

import java.io.FileReader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

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

    public ShoppingList(String id, String name, Map<String, Integer> products) {
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
    public static ShoppingList loadFromFile(String fileName){
        System.out.println(fileName);
        JSONParser parser = new JSONParser();
        //String fileDirectory = "client/lists/" + fileName;

        try (FileReader reader = new FileReader(fileName)) {
            JSONObject jsonShoppingList = (JSONObject) parser.parse(reader);

            String listID = (String) jsonShoppingList.get("id");
            String name = (String) jsonShoppingList.get("name");

            Map<String, Integer> fileProducts = new HashMap<>();
            JSONArray productsArray = (JSONArray) jsonShoppingList.get("products");
            for (Object product : productsArray) {
                JSONObject productJson = (JSONObject) product;
                String productName = (String) productJson.get("name");
                System.out.println(productName);
                long productQuantity = (long) productJson.get("quantity");
                //productJson.
                fileProducts.put(productName, (int) productQuantity);
            }

            return new ShoppingList(name, listID, fileProducts);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
        return null;
    }
}
