package states;

import model.ShoppingList;

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

        System.out.println("\t1) Go back");
        System.out.println("\t0) Exit");

        Scanner in = new Scanner(System.in);
        String option = in.nextLine();
        while (!(option.matches("[0-1]"))) {
            System.out.println("Invalid option");
            option = in.nextLine();
        }
        switch (option) {
            case "1":
                return new HubState();
            case "0":
                return null;
        }

        return null;
    }
}
