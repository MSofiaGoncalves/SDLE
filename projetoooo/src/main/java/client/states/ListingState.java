package client.states;

import client.Session;
import client.model.ShoppingList;
import client.utils.TablePrinter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ListingState implements State {
    public State step() {
        breakLn();
        printTitle("Your Lists");

        List<ShoppingList> lists = Session.getSession().getLists();

        if (lists.isEmpty()) {
            System.out.println("\nYou don't have any lists yet!");
        }
        else {
            List<List<String>> table = new ArrayList<>();
            table.add(List.of("Name", "ID"));
            for (ShoppingList list : lists) {
                table.add(List.of(list.getName(), list.getId()));
            }
            TablePrinter.printTable(table);
        }

        return displayOptions(List.of("Go back", "Exit"), new ArrayList<>(Arrays.asList(new HubState(), null)));
    }
}
