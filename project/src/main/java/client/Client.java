package client;

import client.states.HubState;
import client.states.State;

public class Client {
    public static void main(String[] args) {
        State state = new HubState();

        while (state != null) {
            state = state.step();
        }
    }
}