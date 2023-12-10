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

        List<ShoppingList> lists = Session.getSession().getActiveLists();

        if (lists.isEmpty()) {
            System.out.println("\nYou don't have any lists yet!");
        }
        else {
            List<List<String>> table = new ArrayList<>();
            table.add(List.of("Index", "Name", "ID"));
            int index = 0;
            while(index < lists.size()) {
                ShoppingList list = lists.get(index);
                table.add(List.of(Integer.toString(index), list.getName(), list.getId()));
                index ++;
            }
            TablePrinter.printTable(table);
        }

        return displayOptions(List.of("View List", "Delete List", "Go back", "Exit"), new ArrayList<>(Arrays.asList(new ListIndexIDState(lists, "view"), new ListIndexIDState(lists, "delete"), new HubState(), null)));
    }
}
