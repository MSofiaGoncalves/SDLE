package client.model;

public class ProductQuantity {
    private int quantity;
    private int quantityBought;

    public ProductQuantity(int quantity, int quantityBought) {
        this.quantity = quantity;
        this.quantityBought = quantityBought;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public int getQuantityBought() {
        return quantityBought;
    }

    public void setQuantityBought(int quantityBought) {
        this.quantityBought = quantityBought;
    }

    public void addToList(int quantity) {
        this.quantity += quantity;
    }

    public void buyQuantity(int quantity) {
        if (this.quantity >= quantity) {
            this.quantity -= quantity;
            this.quantityBought += quantity;
        } else {
            this.quantityBought = this.quantity;
            this.quantity = 0;
        }
    }

    @Override
    public String toString() {
        return "(" + quantity + ", " + quantityBought + ')';
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) return false;
        if (!(obj instanceof ProductQuantity other)) return false;
        return this.quantity == other.quantity && this.quantityBought == other.quantityBought;
    }
}
