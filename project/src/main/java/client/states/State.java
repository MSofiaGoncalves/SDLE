package client.states;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public interface State {
    State step();

    default void printTitle(String title) {
        System.out.print("\t");
        for (int i = 0; i < title.length() + 4; i++) {
            System.out.print("*");
        }
        System.out.print("\n");

        System.out.println("\t* " + title + " *");

        System.out.print("\t");
        for (int i = 0; i < title.length() + 4; i++) {
            System.out.print("*");
        }
        System.out.print("\n");
    }

    default void printLine(String message){
        System.out.print("\n");
        System.out.println("\t* " + message);
    }

    default void breakLn() {
        for (int i = 0; i < 50; i++) System.out.print("\n");
        try {
            if (System.getProperty("os.name").contains("Windows")) {
                new ProcessBuilder("cmd", "/c", "cls").inheritIO().start().waitFor();
            } else {
                new ProcessBuilder("bash", "-c", "clear").inheritIO().start().waitFor();
            }
        } catch (Exception ex) {
            for (int i = 0; i < 50; i++) System.out.print("\n");
        }
    }

    default State displayOptions(List<String> options, ArrayList<State> states) {
        for (int i = 0; i < options.size(); i++) {
            System.out.println("\t" + (options.size() - i - 1) + ") " + options.get(i));
        }

        Scanner in = new Scanner(System.in);
        String option = in.nextLine();
        while (!(option.matches("[0-" + (options.size() - 1) + "]"))) {
            System.out.println("Invalid option");
            option = in.nextLine();
        }
        return states.get(options.size() - 1 - Integer.parseInt(option));
    }

}
