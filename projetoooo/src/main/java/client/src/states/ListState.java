package client.src.states;

import java.util.Scanner;

public class ListState implements State {
    public State step() {
        breakLn();
        printTitle("List #id");
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
