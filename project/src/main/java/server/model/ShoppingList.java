package server.model;

import crdts.utils.Triple;
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

    @BsonProperty("deleted")
    private boolean deleted;

    public ShoppingList() {
        this.products = new HashMap<>();
        this.deleted = false;
    }

    public ShoppingList(String id, String name) {
        this.id = id;
        this.name = name;
        this.products = new HashMap<>();
        this.deleted = false;
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

    public void deleteProduct(String name){
        this.products.remove(name);
        this.addWins.rm(name);
    }

    public void mergeLists(ShoppingList list){
        this.addWins.join(list.getAddWins());
        for(String key : this.products.keySet()){
            if(!this.addWins.containsProduct(key)) {
                this.deleteProduct(key);
            }
        }

        for(Triple<String, String, Long> triple : list.getAddWins().getSet()){
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
}