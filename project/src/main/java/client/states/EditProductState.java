package client.states;

import client.model.ShoppingList;

import java.util.Scanner;

public class EditProductState implements State{
    private ShoppingList shoppingList;
    private String action;
    EditProductState(ShoppingList shoppingList, String action) {
        this.shoppingList = shoppingList;
        this.action = action;
    }
    @Override
    public State step() {
        breakLn();
        printLine("Product Name: ");

        Scanner in = new Scanner(System.in);
        String name = in.nextLine();

        printLine("Quantity to " + action + ": ");
        in = new Scanner(System.in);

        while (!in.hasNextInt()) {
            System.out.println("Input is not a number.");
            in.nextLine();
        }
        Integer quantity = in.nextInt();

        if(action.equals("add")) {
            this.shoppingList.addProductQuantity(name, quantity);
        } else if (action.equals("remove")) {
            this.shoppingList.removeProductQuantity(name, quantity);
        } else{
            this.shoppingList.buyProductQuantity(name, quantity);
        }

        return new ListState(this.shoppingList);
    }
}
