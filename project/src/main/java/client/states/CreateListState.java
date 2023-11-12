package client.states;

import client.Session;
import client.model.ShoppingList;

import java.util.Scanner;

public class CreateListState implements State {
    public State step() {
        breakLn();
        printTitle("Create List");

        System.out.print("Name: ");
        Scanner in = new Scanner(System.in);
        String name = in.nextLine();

        System.out.println("after name read");
        ShoppingList shoppingList = Session.getSession().createList(name);
        System.out.println("after create list");

        return new ListState(shoppingList);
    }
}
