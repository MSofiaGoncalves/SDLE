import states.HubState;
import states.State;

public class Client {
    public static void main(String[] args) {
        State state = new HubState();

        while (state != null) {
            state = state.step();
        }
    }
}