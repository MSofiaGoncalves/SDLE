package client.states;

import client.model.ShoppingList;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

public class DeleteProductState implements State {
    private ShoppingList shoppingList;
    DeleteProductState(ShoppingList shoppingList) {
        this.shoppingList = shoppingList;
    }
    public State step() {
        breakLn();
        printLine("Product Name: ");

        Scanner in = new Scanner(System.in);
        String name = in.nextLine();

        if(!this.shoppingList.hasProduct(name)){
            System.out.println("Invalid product name.");
            return new ListState(this.shoppingList);
        }

        this.shoppingList.deleteProduct(name);

        return new ListState(this.shoppingList);
    }
}
