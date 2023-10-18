package model;

public class ShoppingList {
    private String id;
    private String name;

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
}
