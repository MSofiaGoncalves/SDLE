package model;

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

    public void addProduct(String name, Integer quantity){
        products.put(name, quantity);
    }

    public void removeProduct(String name, Integer quantity){
        products.remove(name);
    }

    public void addQuantity(String name, Integer quantity){
        Integer currQuantity = products.get(name);
        products.put(name, currQuantity+quantity);
    }

    public void removeQuantity(String name, Integer quantity){
        Integer currQuantity = products.get(name);
        if (currQuantity - quantity > 1) {
            products.put(name, currQuantity - quantity);
        } else {
            products.remove(name);
        }
    }

    public JSONObject toJSonObject(){
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
        String fileName = name + ".json";
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
