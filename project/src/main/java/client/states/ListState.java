package client.states;

import client.Session;
import client.model.ShoppingList;

import java.io.File;
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
                System.out.println("List does not exist.");
                return new HubState();
            }
            String directoryPath = "src/main/java/client/lists/" + Session.getSession().getUsername();
            String fileName = listId + ".json"; // Replace this with the file name you want to check

            File directory = new File(directoryPath);
            System.out.println("Directory: " + directory.getAbsolutePath());
            if (directory.exists() && directory.isDirectory()) {
                File file = new File(directory, fileName);

                if (file.exists()) {
                    System.out.println("File exists: " + file.getAbsolutePath());
                } else {
                    shoppingList.saveToFile();
                    Session.getSession().addShoppingList(shoppingList);
                }
            } else {
                System.out.println("Directory does not exist.");
            }
        }

        breakLn();
        printTitle("List " + shoppingList.getName());
        System.out.println("#" + shoppingList.getId());

        System.out.println("This is the view list page. \n\n");

        if (this.shoppingList.hasProducts()) {
            this.shoppingList.printProducts();
        }

        return displayOptions(List.of("Add item", "Delete item", "Add quantity", "Remove quantity",  "Buy quantity", "Go back", "Exit"),
                new ArrayList<>(Arrays.asList(new AddProductState(this.shoppingList), new DeleteProductState(this.shoppingList), new EditProductState(this.shoppingList, "add"), new EditProductState(this.shoppingList, "remove"), new EditProductState(this.shoppingList, "buy"), new HubState(), null)));
    }
}





























