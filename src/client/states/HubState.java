package states;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class HubState implements State {
    public State step() {
        breakLn();
        printTitle("Menu");

        return displayOptions(List.of("Create List", "Open List", "View Lists", "Exit"),
                new ArrayList<>(Arrays.asList(new CreateListState(), new ListState(), new ListingState(), null)));
    }
}
