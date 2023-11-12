package client;

import client.states.LoginState;
import client.states.State;

public class Client {
    public static void main(String[] args) {
        State state = new LoginState();

        while (state != null) {
            state = state.step();
        }
    }
}