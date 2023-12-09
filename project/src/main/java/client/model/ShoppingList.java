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

    //String is the name of the product, Product is the product itself
    private Map<String, Product> products;

    private AddWins addWins;

    public ShoppingList(String name) {
        this.id = java.util.UUID.randomUUID().toString();
        this.name = name;
        this.products = new HashMap<>();
        this.addWins = new AddWins(this.id);
        save();

    }

    public ShoppingList(String id, String name) {
        this.id = id;
        this.name = name;
        this.products = new HashMap<>();
        this.addWins = new AddWins(this.id);
        saveToFile();
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
        System.out.println("Json: " + json);
        String directoryPath = "src/main/java/client/lists/" + username + "/";
        String fileName = directoryPath + this.id + ".json";
        System.out.println("Username: " + username + "file: " + fileName);

        try {
            Path path = Paths.get(fileName);
            System.out.println("path: " + path);
            File directory = path.getParent().toFile();
            System.out.println("directory: " + directory);
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

    public void removeFromFile() {
        String username = Session.getSession().getUsername();
        String directoryPath = "src/main/java/client/lists/" + username + "/";
        String fileName = directoryPath + this.id + ".json";

        CompletableFuture<Void> future = new CompletableFuture<>();

        try {
            Path path = Paths.get(fileName);
            File file = path.toFile();

            if (file.exists()) {
                CompletableFuture.runAsync(() -> {
                    if (file.delete()) {
                        System.out.println("File deleted successfully: " + fileName);
                        future.complete(null);
                    } else {
                        System.out.println("Failed to delete file: " + fileName);
                        future.completeExceptionally(new RuntimeException("Failed to delete file: " + fileName));
                    }
                });
            } else {
                System.out.println("File does not exist: " + fileName);
                future.complete(null);
            }
        } catch (Exception e) {
            System.out.println("Error removing file: " + e.getMessage());
            future.completeExceptionally(e);
        }


    }

    // Define a custom CompletionHandler
    private class MyCompletionHandler implements java.nio.channels.CompletionHandler<Integer, Void> {
        private final AsynchronousFileChannel fileChannel;
        private final String fileName;

        public MyCompletionHandler(AsynchronousFileChannel fileChannel, String fileName) {
            this.fileChannel = fileChannel;
            this.fileName = fileName;
        }

        @Override
        public void completed(Integer result, Void attachment) {
            try {
                // Close the file channel
                fileChannel.close();
                System.out.println("File deleted successfully: " + fileName);
            } catch (Exception e) {
                System.out.println("Error closing file channel: " + e.getMessage());
            }
        }

        @Override
        public void failed(Throwable exc, Void attachment) {
            System.out.println("Failed to remove file: " + fileName + ", Error: " + exc.getMessage());
        }
    }



    public AddWins getAddWins() {
        return addWins;
    }

    //@Override
    /*public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("ShoppingList{id='").append(id).append('\'');
        sb.append(", name='").append(name).append('\'');
        sb.append(", products=").append(products);
        sb.append(", addWins=").append(addWins);
        sb.append('}');
        return sb.toString();
    }*/

    public void mergeListsClient(ShoppingList list){
        this.addWins.join(list.getAddWins());
        System.out.println("Lista dos produtos antes do join NO CLIENT: " + this.products);
        System.out.println(this.products.keySet());
        Set<String> keySet = this.products.keySet();
        for(String key : keySet){
            System.out.println("Key: " + key);
            if(!this.addWins.containsProduct(key)) {
                System.out.println("A remover produto: " + key);
                this.deleteProduct(key);
            }
        }
        for(Triple<String, String, Long> triple : this.addWins.getSet()){
            if(!this.products.containsKey(triple.getSecond())){
                System.out.println("doesnt contain: " + triple.getSecond());
                Product p = new Product(triple.getSecond(), 0);
                this.products.put(p.getName(), p);
            }
        }

        // pn counter:
        // agora sabemos que os produtos no this e na list (que veio do argumento) sao os mesmos
        // fazer loop por this.products
        // dar merge de cada um com os counters dos products da list correspondentes

        for (Map.Entry<String, Product> product : products.entrySet()) {
            System.out.println("Product no merge de quantidades:" + product);
            //Também alterei aqui
            if(this.getProducts().containsKey(product.getKey()) && list.getProducts().containsKey(product.getKey())){
                System.out.println("Key:" + product.getKey());
                //System.out.println("list.getProducts().get(product.getKey()): " + list.getProducts().get(product.getKey()).getPncounter().toString());
                product.getValue().mergeProduct(list.getProducts().get(product.getKey()));
                //System.out.println("Product depois do merge:" + list.getProducts().get(product.getKey().getPnCounter().toString());
            }
        }

        System.out.println("Lista dos produtos depois do join NO CLIENT: " + this.products);
        System.out.println("AddWins do this depois do join NO CLIENT:" + addWins.toString());
    }

    /**
     * Saves the list to the server and local storage. <br>
     * Should be called everytime the list is modified.
     */
    private void save() {
        Session.getConnector().writeList(this);
        System.out.println("save");
        System.out.println(this.getProducts());
        saveToFile();
        System.out.println("finished save to file");
        Session.getSession().addShoppingList(this);
    }

}
