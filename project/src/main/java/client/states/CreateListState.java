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

        ShoppingList shoppingList = Session.getSession().createList(name);
//        shoppingList.saveToFile();

        return new ListState(shoppingList);
//        return new ListState(null);

    }
}
