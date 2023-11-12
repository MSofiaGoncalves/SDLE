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

        System.out.println("USER: " + Session.getSession().getUsername());
        List<ShoppingList> lists = Session.getSession().getLists();

        if (lists.isEmpty()) {
            System.out.println("\nYou don't have any lists yet!");
        }
        else {
            List<List<String>> table = new ArrayList<>();
            table.add(List.of("Index", "Name", "ID"));
            for (int i = 0; i < lists.size(); i++) {
                ShoppingList list = lists.get(i);
                table.add(List.of(Integer.toString(i), list.getName(), list.getId()));
            }
            TablePrinter.printTable(table);
        }

        return displayOptions(List.of("View List", "Go back", "Exit"), new ArrayList<>(Arrays.asList(new ListIndexIDState(lists), new HubState(), null)));
    }
}
