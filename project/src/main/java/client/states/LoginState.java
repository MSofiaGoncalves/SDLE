package client.states;

import client.Session;
import client.model.ShoppingList;
import client.utils.TablePrinter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;


public class LoginState implements State {

    public static String username;

    private static Session instance;
    public State step() {
        breakLn();
        printTitle("Login");

        System.out.print("Username: ");
        Scanner in = new Scanner(System.in);
        String username = in.nextLine();

        Session.setUsername(username);
        return new HubState();
    }
}
