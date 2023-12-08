package server.model;

import crdts.GCounter;
import crdts.PNCounter;

public class Product {
    private String name;
    private PNCounter pnCounter; // related with quantityBought
    private GCounter gCounter; // related with quantity

    public Product(String name, int quantity) {
        this.name = name;
        this.pnCounter = new PNCounter(name);
        this.pnCounter.increment(quantity);
        this.gCounter = new GCounter(name);
    }

    public Product(String name, int quantity, int quantityBought) {
        this.name = name;
        this.pnCounter = new PNCounter(name);
        this.pnCounter.increment(quantity);
        this.gCounter = new GCounter(name);
        this.gCounter.increment(quantityBought);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public PNCounter getPnCounter() {
        return pnCounter;
    }

    public GCounter getGCounter() {
        return gCounter;
    }

    // when adding a product, we increment the quantity and call the PNCounter for it
    public void addQuantity(int value) {
        this.pnCounter.increment(value);
    }

    // when removing a product quantity, we decrement the quantity and call the PNCounter for it
    public void removeQuantity(int value) {
        this.pnCounter.decrement(value);
    }

    // the quantity decreases and quantity bought increases
    public void buyQuantity(int value){
        this.gCounter.increment(value);
        this.pnCounter.decrement(value);
    }

    public int getQuantity(){
        return this.pnCounter.value();
    }

    public int getQuantityBought(){
        return this.gCounter.value();
    }


    public void mergeProduct(Product product){
        this.pnCounter.merge(product.getPnCounter());
        this.gCounter.merge(product.getGCounter());
    }

    /*
    @Override
    public String toString() {
        return "(" + quantity + ", " + quantityBought + ')';
    }*/
}
