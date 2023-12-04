package server.model;

import server.model.Product;
import crdts.AddWins;
import org.bson.codecs.pojo.annotations.BsonId;
import org.bson.codecs.pojo.annotations.BsonProperty;
import server.utils.Pair;

import java.util.HashMap;
import java.util.Map;

public class ShoppingList {
    // making id indexed and unique
    @BsonProperty("id")
    private String id;
    @BsonProperty("name")
    private String name;
    @BsonProperty("products")
    private Map<String, Product> products;
    @BsonProperty("addWins")
    private AddWins addWins;

    public ShoppingList() {
        this.products = new HashMap<>();
    }

    public ShoppingList(String id, String name) {
        this.id = id;
        this.name = name;
        this.products = new HashMap<>();
    }

    public String getId() {
        return this.id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setProducts(Map<String, Product> products) {
        this.products = products;
    }

    public Map<String, Product> getProducts() {
        return products;
    }

    public AddWins getAddWins() {
        return addWins;
    }

    public void setAddWins(AddWins addWins) {
        this.addWins = addWins;
    }

    public void mergeLists(ShoppingList list){
        this.addWins.join(list.getAddWins());
        for (Map.Entry<String, Product> entry : products.entrySet()) {
            entry.getValue().mergeProducts(entry.getValue());

        }

    }
}