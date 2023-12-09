package client.states;

import client.Session;
import client.model.ShoppingList;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

public class ListState implements State {
    private ShoppingList shoppingList;

    ListState() {
    }

    ListState(ShoppingList shoppingList) {
        this.shoppingList = shoppingList;
    }

    public State step() {
        if (shoppingList == null) {
            System.out.print("List id: ");
            Scanner in = new Scanner(System.in);
            String listId = in.nextLine();
            this.shoppingList = Session.getSession().getList(listId);
            if (this.shoppingList == null) { // non existent
                return new HubState();
            }
        }
        Session.getSession().startRefresher(shoppingList.getId());

        breakLn();
        printTitle("List " + shoppingList.getName());
        System.out.println("#" + shoppingList.getId());

        System.out.println("This is the view list page. \n\n");

        if (this.shoppingList.hasProducts()) {
            this.shoppingList.printProducts();
        }

        State s = displayOptions(List.of(
                "Refresh", "Add item", "Delete item", "Add quantity", "Buy quantity", "Go back", "Exit"
                ),
                new ArrayList<>(Arrays.asList(
                        new ListState(),
                        new AddProductState(this.shoppingList),
                        new DeleteProductState(this.shoppingList),
                        new EditProductState(this.shoppingList, "add"),
                        new EditProductState(this.shoppingList, "buy"),
                        new HubState(),
                        null
                )));
        Session.getSession().stopRefresher();

        // refresh list
        if (s instanceof ListState) s = new ListState(Session.getSession().getLocalList(shoppingList.getId()));

        return s;
    }
}





























