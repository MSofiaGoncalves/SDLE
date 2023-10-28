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

        breakLn();
        printTitle("List " + shoppingList.getName());
        System.out.println("#" + shoppingList.getId());

        System.out.println("This is the view list page. \n\n");

        if (this.shoppingList.hasProducts()) {
            this.shoppingList.printProducts();
        }

        return displayOptions(List.of("Add item", "Add quantity", "Buy quantity", "Go back", "Exit"),
                new ArrayList<>(Arrays.asList(new AddProductState(this.shoppingList), new EditProductState(this.shoppingList, "add"), new EditProductState(this.shoppingList, "buy"), new HubState(), null)));
    }
}
