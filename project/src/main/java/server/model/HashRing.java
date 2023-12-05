package server.model;

import server.Store;
import server.connections.NodeConnector;
import server.db.Database;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Defines a consistent hashing ring. <br>
 * Each node maps to a number <strong>virtualNodesNumber</strong> of virtual nodes. <br>
 * The ring is implemented as a TreeMap, where the key is the hash of the node and the value is the node's name. <br>
 * The keys are hashed modulo <strong>size</strong>.
 */
public class HashRing {
    private final TreeMap<Long, String> ring;
    private final int virtualNodesNumber;
    private final int size;
    private int replicas;
    private final ConcurrentHashMap<String, Boolean> nodeStatus;
    private final ConcurrentHashMap<String, Long> hashCache;

    /**
     * Creates a new HashRing.
     *
     * @param virtualNodesNumber The number of virtual nodes for each physical node.
     * @param size               The size of the ring.
     * @param replicas           The number of replicas to store.
     */
    public HashRing(int virtualNodesNumber, int size, int replicas) {
        this.virtualNodesNumber = virtualNodesNumber;
        this.size = size;
        this.replicas = replicas;
        this.nodeStatus = new ConcurrentHashMap<>();
        this.hashCache = new ConcurrentHashMap<>();
        ring = new TreeMap<>();
    }

    /**
     * Creates a new HashRing with the specified nodes.
     *
     * @param nodes              The nodes to add to the ring.
     * @param virtualNodesNumber The number of virtual nodes for each physical node.
     * @param size               The size of the ring.
     * @param replicas           The number of replicas to store.
     */
    public HashRing(String[] nodes, int virtualNodesNumber, int size, int replicas) {
        this(virtualNodesNumber, size, replicas);
        addNodes(nodes);
    }

    /**
     * Adds multiple nodes to the ring.
     *
     * @param nodes The nodes to add.
     */
    public void addNodes(String[] nodes) {
        for (String node : nodes) {
            addNode(node);
        }
    }

    /**
     * Adds a node to the ring. <br>
     * Node's name is hashed once per virtual replica and added to the ring.
     *
     * @param node Node's uri
     */
    public void addNode(String node) {
        for (int i = 0; i < virtualNodesNumber; i++) {
            ring.put(getHash(node + "-" + i), node);
            nodeStatus.put(node, true);
        }
    }

    public void updateNodeStatus(String node, boolean status) {
        if (status) {
            // TODO: recovery
        } else {
            relocateLists(node);
        }
        nodeStatus.put(node, status);
    }


    /**
     * Get all the physical nodes that hold a key
     *
     * @param key The key to get the nodes for.
     * @return The nodes that hold the key.
     */
    public Set<String> getNodes(String key) {
        return new HashSet<>(getNodes(key, replicas));
    }

    private List<String> getNodes(String key, int replicas) {
        Long hash = getHash(key);
        if (!ring.containsKey(hash)) {
            hash = ring.floorKey(hash);
            if (hash == null) {
                hash = ring.lastKey();
            }
        }
        List<String> nodes = new ArrayList<>();
        if (nodeStatus.get(ring.get(hash))) {
            nodes.add(ring.get(hash));
        }
        Long initialHash = hash;
        while (nodes.size() < replicas) {
            hash = ring.higherKey(hash);
            if (hash == null) {
                hash = ring.firstKey();
            }
            if (hash.equals(initialHash)) {
                Store.getLogger().warning("Error: Trying to store " + replicas
                        + " replicas of key " + key + " but only "
                        + nodes.size() + " nodes are available.");
                return List.of();
            }
            if (!nodes.contains(ring.get(hash)) && nodeStatus.get(ring.get(hash))) {
                nodes.add(ring.get(hash));
            }
        }

        return nodes;
    }

    /**
     * Get the hash of a node. <br>
     * This is done using MD5 and rehashing with specified desired size.
     *
     * @param node The node to hash.
     * @return The hash of the node.
     */
    private long getHash(String node) {
        if (hashCache.containsKey(node)) {
            return hashCache.get(node);
        }
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            md.update(node.getBytes());

            byte[] hash = md.digest();
            BigInteger bigInt = new BigInteger(1, hash);
            BigInteger result = bigInt.mod(BigInteger.valueOf(size));

            // result to hex string
            hashCache.put(node, result.longValue());
            return result.longValue();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return -1;
    }

    private void relocateLists(String node) {
        Map<String, List<ShoppingList>> toSend = new HashMap<>();
        List<ShoppingList> lists = Database.getInstance().readAllLists();
        for (ShoppingList list : lists) {
            List<String> nodes = getNodes(list.getId(), replicas);
            if (nodes.contains(node)) {
                List<String> nextNodes = getNodes(list.getId(), replicas + 1);
                if (nextNodes.isEmpty()) {
                    Store.getLogger().warning("Error: No nodes available to relocate list " + list.getId());
                    continue;
                }
                String nextNode = nextNodes.get(nextNodes.size() - 1);
                toSend.computeIfAbsent(nextNode, k -> new ArrayList<>());
                toSend.get(nextNode).add(list);
            }
        }

        for (String url : toSend.keySet()) {
            new NodeConnector(url).sendHintedHandoff(node, toSend.get(url));
        }
    }
}
