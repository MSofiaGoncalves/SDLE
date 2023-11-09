package server;

import org.zeromq.ZContext;
import org.zeromq.ZMQ;
import server.LoggerFormatter;

import java.io.IOException;
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
    private static java.util.concurrent.ExecutorService threadPool;

    private static Logger logger;

    private Store() {
        try {
            context = new ZContext();
            socket = context.createSocket(ZMQ.ROUTER);
            socket.bind("tcp://*:5555");
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        int numThreads = Runtime.getRuntime().availableProcessors(); // use one thread per CPU core
        threadPool = Executors.newFixedThreadPool(numThreads);

        initLogger();
        logger.info("initLogger done.");
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
            FileHandler fileHandler = new FileHandler("logs.log", true);
            fileHandler.setFormatter(new LoggerFormatter());
            logger.addHandler(fileHandler);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static Logger getLogger() {
        logger.info("Inside getLogger");
        return logger;
    }
}
