package client.states;

import client.Session;
import client.model.ShoppingList;

import java.util.Scanner;

public class DeleteListState implements State {
    private final ShoppingList shoppingList;
    DeleteListState(ShoppingList shoppingList) {
        this.shoppingList = shoppingList;
    }

    public State step() {
        breakLn();
        String name = shoppingList.getName();

        shoppingList.deleteList();


        return new HubState();
    }
}
