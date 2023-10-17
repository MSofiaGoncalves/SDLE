import model.List;

public class Session {
    private static Session instance;

    private Session() {

    }

    /**
     * Creates a list.
     * Saves it to local storage and sends it to the server.
     * @return Newly created list object.
     */
    public List createList() {
        return new List("123");
    }

    public static synchronized Session getSession() {
        if (instance == null) {
            instance = new Session();
        }
        return instance;
    }
}
