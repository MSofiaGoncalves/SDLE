package project.client;

import project.client.states.HubState;
import project.client.states.State;

public class Client {
    public static void main(String[] args) {
        State state = new HubState();

        while (state != null) {
            state = state.step();
        }
    }
}