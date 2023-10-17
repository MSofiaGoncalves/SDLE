package states;

import java.util.Scanner;

public class HubState implements State {
    public State step() {
        breakLn();
        printTitle("Lobby");

        System.out.println("\n\t1) Create List");
        System.out.println("\t2) Open List");
        System.out.println("\t3) View Lists");
        System.out.println("\t0) Exit");

        Scanner in = new Scanner(System.in);
        String option = in.nextLine();
        while (!(option.matches("[0-4]"))) {
            System.out.println("Invalid option");
            option = in.nextLine();
        }

        switch (option) {
            case "1":
                return new CreateListState();
            case "2":
                return new ListState();
            case "3":
                return new ListsListState();
            case "0":
                return null;
        }
        return null;
    }
}
