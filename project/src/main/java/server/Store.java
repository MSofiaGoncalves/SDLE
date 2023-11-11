package server;

import org.zeromq.ZContext;
import org.zeromq.ZMQ;

import java.io.FileInputStream;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Properties;
import java.util.concurrent.Executors;
import java.util.logging.Logger;
import java.util.logging.ConsoleHandler;
import java.util.logging.FileHandler;


/**
 * Singleton class that holds the current session.
 */
public class Store {
    private static Store instance = null;
    private ZContext context;
    private ZMQ.Socket clientBroker;
    private java.util.concurrent.ExecutorService threadPool;
    private Properties properties;

    private static Logger logger;

    private Store() {
        initProperties();
    }

    public void initConnections() {
        String[] temp = getProperty("clienthost").split(":");
        setProperty("id", temp[temp.length - 1]);

        initLogger();

        try {
            context = new ZContext();
            clientBroker = context.createSocket(ZMQ.ROUTER);
            clientBroker.bind(getProperty("clienthost"));
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        int numThreads = Runtime.getRuntime().availableProcessors(); // use one thread per CPU core
        threadPool = Executors.newFixedThreadPool(numThreads);
    }

    public static Store getInstance() {
        if (instance == null) {
            instance = new Store();
        }

        return instance;
    }

    public void execute(Runnable runnable) {
        threadPool.execute(runnable);
    }

    public ZMQ.Socket getClientBroker() {
        if (clientBroker == null) {
            getInstance();
        }
        return clientBroker;
    }

    public ZContext getContext() {
        if (context == null) {
            getInstance();
        }
        return context;
    }

    private void initLogger() {
        logger = Logger.getLogger("server");
        logger.setUseParentHandlers(false);
        ConsoleHandler consoleHandler = new ConsoleHandler();
        consoleHandler.setFormatter(new LoggerFormatter());
        logger.addHandler(consoleHandler);

        try {
            Files.createDirectories(Paths.get("logs/"));
            System.out.println(getProperty("clienthost"));
            FileHandler fileHandler = new FileHandler("logs/" + getProperty("id") + ".log", true);
            fileHandler.setFormatter(new LoggerFormatter());
            logger.addHandler(fileHandler);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void initProperties() {
        if (properties == null)
            properties = new Properties();
        final String filePath = "src/main/java/server/server.properties";
        try {
            properties.load(new FileInputStream(filePath));
        } catch (Exception e) {
            throw new RuntimeException("Unable to load properties file: " + filePath, e);
        }
    }

    public String getProperty(String key) {
        return properties.getProperty(key);
    }

    public void setProperty(String key, String value) {
        properties.setProperty(key, value);
    }

    public static Logger getLogger() {
        if (instance == null) {
            getInstance();
        }
        return logger;
    }

}
