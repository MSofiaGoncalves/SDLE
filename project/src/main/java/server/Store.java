package server;

import org.zeromq.ZContext;
import org.zeromq.ZMQ;

import java.util.concurrent.Executors;

/**
 * Singleton class that holds the current session.
 */
public class Store {
    private static Store instance = null;
    private static ZContext context;
    private static ZMQ.Socket socket;
    private static java.util.concurrent.ExecutorService threadPool;

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
}
