package states;

import model.ShoppingList;
import utils.TablePrinter;

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
//        this.shoppingList.saveToFile();

        return displayOptions(List.of("Add another item", "Go back", "Exit"), new ArrayList<>(Arrays.asList(new AddProductState(this.shoppingList), new ListState(this.shoppingList), null)));
    }
}
