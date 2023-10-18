package states;

import model.ShoppingList;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

public class ListState implements State {
    private ShoppingList shoppingList;
    ListState() {}
    ListState(ShoppingList shoppingList) {
        this.shoppingList = shoppingList;
    }

    public State step() {
        if (shoppingList == null) {
            System.out.print("List id: ");
            Scanner in = new Scanner(System.in);
            String listId = in.nextLine();
            this.shoppingList = client.Session.getSession().getList(listId);
            if (this.shoppingList == null) { // non existent
                return new HubState();
            }
        }

        breakLn();
        printTitle("List " + shoppingList.getName());
        System.out.println("#" + shoppingList.getId());

        System.out.println("This is the view list page. \n\n");

        return displayOptions(List.of("Go back", "Exit"), new ArrayList<>(Arrays.asList(new HubState(), null)));
    }
}
