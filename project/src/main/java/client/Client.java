package client;

import client.states.HubState;
import client.states.State;
import org.zeromq.ZContext;
import org.zeromq.ZMQ;
import client.model.ShoppingList;

public class Client {
    public static void main(String[] args) {
        //String id = "0cbc34a0-e6da-4119-a27f-74c88bfd84a9";
        String id = "olatesting";
        //ShoppingList shoppingList = new ShoppingList("testing");
        ShoppingList shoppingList = new ShoppingList(id, "testing");
        shoppingList.addProduct("banana", 3);
        Session.getConnector().insertList(shoppingList);
        System.out.println("Inserted");
        ShoppingList list = Session.getConnector().getList(id);

        System.out.println("=====");
        System.out.println(list);
        if (list != null) {
        System.out.println(list.getId());
        System.out.println(list.getName());
        System.out.println(list.getProducts());
        }
        System.out.println("=====");

        /*
        State state = new HubState();

        while (state != null) {
            state = state.step();
        }
        */
    }
}