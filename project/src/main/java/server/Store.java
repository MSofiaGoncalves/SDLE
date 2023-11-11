package server;

import org.zeromq.ZContext;
import org.zeromq.ZMQ;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
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
    private static ZContext context;
    private static ZMQ.Socket socket;
    private static String port;
    private static java.util.concurrent.ExecutorService threadPool;
    private static Properties properties;

    private static Logger logger;

    private Store() {
        initProperties();
        // TODO: read port from config file
        port = "5555";
        try {
            context = new ZContext();
            socket = context.createSocket(ZMQ.ROUTER);
            socket.bind("tcp://" + getProperty("serverhost") + ":" + port);
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        int numThreads = Runtime.getRuntime().availableProcessors(); // use one thread per CPU core
        threadPool = Executors.newFixedThreadPool(numThreads);

        initLogger();
    }

    public static Store getInstance() {
        if (instance == null) {
            instance = new Store();
        }

        return instance;
    }

    public static void execute(Runnable runnable) {
        threadPool.execute(runnable);
    }

    public static ZMQ.Socket getSocket() {
        if (socket == null) {
            getInstance();
        }
        return socket;
    }

    private void initLogger() {
        logger = Logger.getLogger("server");
        logger.setUseParentHandlers(false);
        ConsoleHandler consoleHandler = new ConsoleHandler();
        consoleHandler.setFormatter(new LoggerFormatter());
        logger.addHandler(consoleHandler);

        try {
            Files.createDirectories(Paths.get("logs/"));
            FileHandler fileHandler = new FileHandler("logs/" + port + ".log", true);
            fileHandler.setFormatter(new LoggerFormatter());
            logger.addHandler(fileHandler);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void initProperties() {
        properties = new Properties();
        final String filePath = "src/main/java/server/server.properties";
        try {
            properties.load(new FileInputStream(filePath));
        } catch (Exception e) {
            throw new RuntimeException("Unable to load properties file: " + filePath, e);
        }
    }

    public static String getProperty(String key) {
        if (instance == null) {
            getInstance();
        }
        return properties.getProperty(key);
    }

    public static Logger getLogger() {
        if (instance == null) {
            getInstance();
        }
        return logger;
    }

}
