package client.states;

import client.model.ShoppingList;

import java.util.List;
import java.util.Scanner;

public class ListIndexIDState implements State{
    private final List<ShoppingList> lists;
    private String action;
    ListIndexIDState(List<ShoppingList> lists, String action) {
        this.lists = lists;
        this.action = action;
    }
    @Override
    public State step() {
        printLine("List index: ");
        Scanner in = new Scanner(System.in);

        while (!in.hasNextInt()) {
            System.out.println("Input is not a number.");
            in.nextLine();
        }
        int index = in.nextInt();
        if(index >= lists.size()){
            System.out.println("Index out of range.");
            return new ListIndexIDState(lists, action);
        }
        if(action.equals("view")) {
            return new ListState(lists.get(index));
        }
        else{
            return new DeleteListState(lists.get(index));
        }
    }
}
