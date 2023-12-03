package client.model;

public class Product {
    //private int quantity;
    //private int quantityBought;
    private ProductQuantity productQuantity;
    private String name;

    public Product(String name, ProductQuantity productQuantity) {
        this.name = name;
        //this.quantity = quantity;
        //this.quantityBought = quantityBought;
        this.productQuantity = productQuantity;
    }

    public ProductQuantity getProductQuantity() {
        return productQuantity;
    }

    public void setProductQuantity(ProductQuantity productQuantity) {
        this.productQuantity = productQuantity;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    /*
    @Override
    public String toString() {
        return "(" + quantity + ", " + quantityBought + ')';
    }*/
}
