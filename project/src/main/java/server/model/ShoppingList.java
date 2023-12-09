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

    @BsonProperty("flag")
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
        System.out.println("Merging lists");
        System.out.println("This: " + this.products);
        System.out.println("List: " + list.getProducts());
        System.out.println("AddWins do this:" + addWins.toString());
        System.out.println("AddWins do list:" + list.getAddWins().toString());
        this.addWins.join(list.getAddWins());
        System.out.println("AddWins do this depois do join NO SERVER:" + addWins.toString());
        for(String key : this.products.keySet()){
            if(!this.addWins.containsProduct(key)) {
                System.out.println("A remover produto: " + key);
                this.deleteProduct(key);
            }
        }
        // iterar pelo set e inserir se nao estiver no this
        for(Triple<String, String, Long> triple : list.getAddWins().getSet()){
            if(!this.products.containsKey(triple.getSecond())){
                System.out.println("A inserir produto: " + triple.getSecond());
                Product p = new Product(triple.getSecond(), 0);
                this.products.put(p.getName(), p);
            }
        }

        for (Map.Entry<String, Product> product : products.entrySet()) {
            System.out.println("A DAR MERGE DE QUANTIDADES!!!");
            System.out.println("Product no merge de quantidades:" + product);
            //Alterei aqui, têm ambas de ter o elemento
            if(this.getProducts().containsKey(product.getKey()) && list.getProducts().containsKey(product.getKey())){
                System.out.println("ENTROU NO IF");
                System.out.println("Key:" + product.getKey());
                System.out.println("product.getValue(): " + product.getValue().toString());
                //System.out.println("list.getProducts().get(product.getKey()): " + list.getProducts().get(product.getKey()).getPncounter().toString());
                product.getValue().mergeProduct(list.getProducts().get(product.getKey()));
                //System.out.println("Product depois do merge:" + list.getProducts().get(product.getKey().getPnCounter().toString());
            }
        }

        System.out.println("Lista dos produtos depois do join: " + this.products);
        System.out.println("AddWins do this depois do join:" + addWins.toString());
    }
}