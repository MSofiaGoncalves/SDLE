package states;

import model.ShoppingList;
import utils.TablePrinter;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class ListingState implements State {
    public State step() {
        breakLn();
        printTitle("Your Lists");

        List<ShoppingList> lists = client.Session.getSession().getLists();

        if (lists.size() == 0) {
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

        System.out.println("\n\t1) Go back");
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
