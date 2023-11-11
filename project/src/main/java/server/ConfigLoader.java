package server;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class ConfigLoader {

    public static String serverHost;

    public static Properties loadProperties(String filePath) {
        Properties properties = new Properties();
        try (InputStream is = ConfigLoader.class.getClassLoader().getResourceAsStream(filePath)) {
            if (is != null) {
                properties.load(is);
            } else {
                System.err.println("Unable to find the file: " + filePath);
            }
        } catch (IOException e) {
            e.printStackTrace(); // Handle exception appropriately
        }
        return properties;
    }

    public static void initializeServer()  {
        // Load server.properties
        Properties serverProperties = loadProperties("server/server.properties");
        // Use properties from server.properties
        serverHost = serverProperties.getProperty("serverhost");
        //int serverPort = Integer.parseInt(serverProperties.getProperty("server.port"));

        System.out.println("Server Host no ConfigLoader: " + serverHost);
        //System.out.println("Server Port: " + serverPort);

        // Load client.properties
        /*Properties clientProperties = loadProperties("client/client.properties");

        // Use properties from client.properties
        String serverUrl = clientProperties.getProperty("client.server.url");

        System.out.println("Server URL: " + serverUrl);*/
    }


}

