package client.states;

import client.Session;
import client.model.ShoppingList;

import java.io.BufferedReader;
import java.io.InputStreamReader;
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
                return new HubState();
            }
        }
        Session.getSession().startRefresher(shoppingList.getId());

        breakLn();
        printTitle("List " + shoppingList.getName());
        System.out.println("#" + shoppingList.getId());

        System.out.println("This is the view list page. \n\n");

        if (this.shoppingList.hasProducts()) {
            this.shoppingList.printProducts();
        }

        return waitCompletion();
    }

    /**
     * Completion for this state is done either when the user writes or
     * the list was updated.
     */
    private State waitCompletion() {
        final State[] s = new State[1];

        Thread readerThread = getReaderThread(s);
        Thread checkUpdatesThread = getCheckUpdatesThread(s, readerThread);

        readerThread.start();
        checkUpdatesThread.start();

        try {
            readerThread.join();
            checkUpdatesThread.interrupt();
        } catch (InterruptedException ignored) {
        }

        Session.getSession().stopRefresher();

        return s[0];
    }

    /**
     * Creates the thread that reads the user input. <br>
     * This is done differently on this state in order to have automatic refresh.
     */
    private Thread getReaderThread(State[] s) {
        return new Thread(() -> {
            List<String> options = List.of("Add item", "Delete item", "Add quantity", "Buy quantity", "Go back", "Exit");
            List<State> states = new ArrayList<>(Arrays.asList(
                    new AddProductState(this.shoppingList),
                    new DeleteProductState(this.shoppingList),
                    new EditProductState(this.shoppingList, "add"),
                    new EditProductState(this.shoppingList, "buy"),
                    new HubState(),
                    null
            ));
            for (int i = 0; i < options.size(); i++) {
                System.out.println("\t" + (options.size() - i - 1) + ") " + options.get(i));
            }

            BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
            while (!Thread.interrupted()) {
                try {
                    if (reader.ready()) {
                        String option = reader.readLine();
                        while (!(option.matches("[0-" + (options.size() - 1) + "]"))) {
                            System.out.println("Invalid option");
                            option = reader.readLine();
                        }
                        s[0] = states.get(options.size() - 1 - Integer.parseInt(option));
                        break;
                    }
                } catch (Exception ignored) {
                }
            }
        });
    }

    /**
     * Creates the thread that checks for updates on the list. <br>
     * If one is found, interrupts the reader thread.
     */
    private Thread getCheckUpdatesThread(State[] s, Thread readerThread) {
        return new Thread(() -> {
            while (s[0] == null) {
                try {
                    if (!this.shoppingList.equals(Session.getSession().getLocalList(shoppingList.getId()))) {
                        s[0] = new ListState(Session.getSession().getLocalList(shoppingList.getId()));
                        readerThread.interrupt();
                    }
                    Thread.sleep(Long.parseLong(Session.getSession().getProperty("refreshTime")));
                } catch (InterruptedException ignored) {
                }
            }
        });
    }
}





























