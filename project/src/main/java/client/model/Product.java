package client.model;

import crdts.GCounter;
import crdts.PNCounter;

public class Product {
    private String name;
    public PNCounter pnCounter; // related with quantityBought
    public GCounter gCounter; // related with quantity

    public Product(String name, int quantity) {
        this.name = name;
        this.pnCounter = new PNCounter(name);
        this.pnCounter.increment(quantity);
        this.gCounter = new GCounter(name);
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
        this.pnCounter.merge(product.getPncounter());
        this.gCounter.merge(product.getGCounter());
    }

    public GCounter getGCounter() {
        return this.gCounter;
    }

    public PNCounter getPncounter() {
        return this.pnCounter;
    }

    /*
    @Override
    public String toString() {
        return "(" + quantity + ", " + quantityBought + ')';
    }*/
}
