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

    public Product(String name, PNCounter pnCounter, GCounter gCounter) {
        this.name = name;
        this.pnCounter = pnCounter;
        this.gCounter = gCounter;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Product{name='").append(name).append('\'');
        sb.append(", pnCounter=").append(pnCounter);
        sb.append(", gCounter=").append(gCounter);
        sb.append('}');
        return sb.toString();
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
        System.out.println("No mergeProduct");
        System.out.println("This: " + this.getName());
        //System.out.println("Product: " + product.getName());
        this.pnCounter.merge(product.getPnCounter());
        this.gCounter.merge(product.getGCounter());
    }

    /*
    @Override
    public String toString() {
        return "(" + quantity + ", " + quantityBought + ')';
    }*/
}
