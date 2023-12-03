package client.states;

import client.Session;
import client.model.ShoppingList;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

public class AddProductState implements State {
    private ShoppingList shoppingList;
    AddProductState(ShoppingList shoppingList) {
        this.shoppingList = shoppingList;
    }
    public State step() {
        breakLn();
        printLine("Product Name: ");

        Scanner in = new Scanner(System.in);
        String name = in.nextLine();

        printLine("Product Quantity: ");

        in = new Scanner(System.in);

        while (!in.hasNextInt()) {
            System.out.println("Input is not a number.");
            in.nextLine();
        }
        Integer quantity = in.nextInt();

        this.shoppingList.addProduct(name, quantity);

        return new ListState(this.shoppingList);
    }
}
