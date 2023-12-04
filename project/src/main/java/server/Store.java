package server;

import org.zeromq.ZContext;
import org.zeromq.ZMQ;
import server.model.HashRing;
import server.model.QuorumStatus;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
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
    private ZMQ.Socket nodeBroker;
    private List<String> nodes;

    private java.util.concurrent.ExecutorService threadPool;
    private static Properties properties;

    private HashRing hashRing;
    private ConcurrentHashMap<String, QuorumStatus> quorums;
    private ConcurrentHashMap<String, byte[]> ongoingRedirects;
    private ConcurrentHashMap<String, Instant> waitingReply; // used for failure detection

    public static Logger logger;

    private Store() {
        quorums = new ConcurrentHashMap<>();
        ongoingRedirects = new ConcurrentHashMap<>();
        waitingReply = new ConcurrentHashMap<>();
        initProperties();
    }

    /**
     * Creates a client router and connects to all nodes. <br>
     * Should be called <strong>once</strong> when the server starts.
     */
    public void initConnections() {
        if (context != null) {
            return;
        }

        initHosts();

        try {
            context = new ZContext();

            // Create client broker
            clientBroker = context.createSocket(ZMQ.ROUTER);
            clientBroker.bind(getProperty("clienthost"));

            // Connect to all nodes
            connectNodes();

            logger.info("Listening for clients at " + getProperty("clienthost") + ".");
            logger.info("Listening for nodes at " + getProperty("nodehost") + ".");

            hashRing = new HashRing(getProperty("nodes").split(";"),
                    Integer.parseInt(getProperty("virtualNodes")),
                    Integer.parseInt(getProperty("ringSize")),
                    Integer.parseInt(getProperty("quorumNumber")));
        } catch (Exception e) {
            logger.severe(e.getMessage());
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

    public void sendNodeMessage(String nodeAddress, String message) {
        nodeBroker.sendMore(nodeAddress.getBytes(ZMQ.CHARSET));
        nodeBroker.send(message);
    }

    public HashRing getHashRing() {
        return hashRing;
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

    public ZMQ.Socket getNodeBroker() {
        if (nodeBroker == null) {
            getInstance();
        }
        return nodeBroker;
    }

    public ZContext getContext() {
        if (context == null) {
            getInstance();
        }
        return context;
    }

    public static void initLogger() {
        logger = Logger.getLogger("server");
        logger.setUseParentHandlers(false);
        ConsoleHandler consoleHandler = new ConsoleHandler();
        consoleHandler.setFormatter(new LoggerFormatter());
        logger.addHandler(consoleHandler);

        try {
            Files.createDirectories(Paths.get("logs/"));
            FileHandler fileHandler = new FileHandler("logs/" + getProperty("id") + ".log", true);
            fileHandler.setFormatter(new LoggerFormatter());
            logger.addHandler(fileHandler);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public List<String> getNodes() {
        return nodes;
    }

    public ConcurrentHashMap<String, QuorumStatus> getQuorums() {
        return quorums;
    }

    public ConcurrentHashMap<String, byte[]> getOngoingRedirects() {
        return ongoingRedirects;
    }

    public ConcurrentHashMap<String, Instant> getWaitingReply() {
        return waitingReply;
    }

    public static String getProperty(String key) {
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

    /**
     * Loads the properties file.
     */
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

    /**
     * Sets port numbers and defaults hostnames if not defined.
     */
    private void initHosts() {
        if (getProperty("clienthost") == null) {
            setProperty("clienthost", getProperty("clienthostdefault"));
        }
        if (getProperty("nodehost") == null) {
            setProperty("nodehost", getProperty("nodehostdefault"));
        }

        String[] temp = getProperty("clienthost").split(":");
        setProperty("clientPort", temp[temp.length - 1]);
        // Id of a node is defined by the port it listens for clients
        setProperty("id", temp[temp.length - 1]);

        temp = getProperty("nodehost").split(":");
        setProperty("nodePort", temp[temp.length - 1]);
    }

    /**
     * Connects to all nodes specified in the properties file. <br>
     * <p>
     * Inter-node communication is done using ROUTER-ROUTER sockets.
     */
    private void connectNodes() {
        nodes = List.of(getProperty("nodes").split(";"));
        nodeBroker = context.createSocket(ZMQ.ROUTER);
        nodeBroker.bind(getProperty("nodehost"));
        nodeBroker.setIdentity(getProperty("nodehost").getBytes(ZMQ.CHARSET));
        for (String nodeUrl : nodes) {
            if (nodeUrl.equals(getProperty("nodehost"))) {
                continue;
            }

            nodeBroker.connect(nodeUrl);
        }
    }
}
