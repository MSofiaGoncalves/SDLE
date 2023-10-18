package states;

import model.ShoppingList;

import java.util.Scanner;

public class CreateListState implements State {
    public State step() {
        breakLn();
        printTitle("Create List");

        System.out.print("Name: ");
        Scanner in = new Scanner(System.in);
        String name = in.nextLine();

        ShoppingList shoppingList = client.Session.getSession().createList(name);

        return new ListState(shoppingList);
    }
}
