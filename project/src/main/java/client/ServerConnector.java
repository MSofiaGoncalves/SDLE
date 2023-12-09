package client;

import client.model.ShoppingList;
import com.google.gson.Gson;
import org.zeromq.ZContext;
import org.zeromq.ZMQ;

import java.io.FileInputStream;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Handles the connection to the server.
 */
public class ServerConnector {
    private ZContext context;
    private ZMQ.Socket socket;

    private Properties properties;
    private ConcurrentHashMap<String, ZMQ.Socket> nodes;

    /**
     * Creates a zmq socket and connects to the server.
     */
    public ServerConnector() {
        try {
            context = new ZContext();
            initProperties();
            initHosts();
            socket = context.createSocket(ZMQ.REQ);
            for (String host : getProperty("nodes").split(";")) {
                socket.connect(host);
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    /**
     * Sends a request to the server to write a list.
     * @param shoppingList The list to write.
     */
    public void writeList(ShoppingList shoppingList) {
        String request = String.format("{\"method\":\"write\", \"list\":%s}", new Gson().toJson(shoppingList));
        socket.send(request.getBytes(ZMQ.CHARSET), 0);

        byte[] reply = socket.recv(0);
    }

    /**
     * Sends a request to the server to get a list.
     * @param id The id of the list to get.
     * @return The list with the given id.
     */
    public ShoppingList readList(String id) {
        String request = String.format("{\"method\":\"read\", \"listId\":\"%s\"}", id);
        socket.send(request.getBytes(ZMQ.CHARSET), 0);

        byte[] reply = socket.recv(0);
        return new Gson().fromJson(new String(reply, ZMQ.CHARSET), ShoppingList.class);
    }

    public String getProperty(String key) {
        return properties.getProperty(key);
    }

    private void initProperties() {
        if (properties == null)
            properties = new Properties();
        final String filePath = "src/main/java/client/client.properties";
        try {
            properties.load(new FileInputStream(filePath));
        } catch (Exception e) {
            throw new RuntimeException("Unable to load properties file: " + filePath, e);
        }
    }

    public void setProperty(String key, String value) {
        properties.setProperty(key, value);
    }


    private void initHosts() {
        if (getProperty("serverhost") == null) {
            setProperty("serverhost", getProperty("serverhostdefault"));
        }

        String[] temp = getProperty("serverhost").split(":");
        setProperty("serverPort", temp[temp.length - 1]);
    }
}
