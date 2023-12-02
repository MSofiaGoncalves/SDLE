package server.model;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeMap;

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
    private final int replicas;

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
     * Adds a node to the ring. <br>
     * Node's name is hashed once per virtual replica and added to the ring.
     *
     * @param node Node's uri
     */
    public void addNode(String node) {
        for (int i = 0; i < virtualNodesNumber; i++) {
            ring.put(getHash(node + "-" + i), node);
        }
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
     * Removes a node from the ring.
     *
     * @param node The node to remove.
     */
    public void removeNode(String node) {
        for (int i = 0; i < virtualNodesNumber; i++) {
            ring.remove(getHash(node + "-" + i));
        }
    }

    /**
     * Get all the physical nodes that hold a key
     *
     * @param key The key to get the nodes for.
     * @return The nodes that hold the key.
     */
    public Set<String> getNodes(String key) {
        Long hash = getHash(key);
        if (!ring.containsKey(hash)) {
            hash = ring.floorKey(hash);
            if (hash == null) {
                hash = ring.lastKey();
            }
        }
        Set<String> nodes = new HashSet<>();
        nodes.add(ring.get(hash));
        Long initialHash = hash;
        while (nodes.size() < replicas) {
            hash = ring.higherKey(hash);
            if (hash == null) {
                hash = ring.firstKey();
            }
            if (hash.equals(initialHash)) {
                throw new RuntimeException("Error: Trying to store " + replicas
                        + " replicas of key " + key + " but only "
                        + nodes.size() + " nodes are available.");
            }
            if (!nodes.contains(ring.get(hash))) {
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
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            md.update(node.getBytes());

            byte[] hash = md.digest();
            BigInteger bigInt = new BigInteger(1, hash);
            BigInteger result = bigInt.mod(BigInteger.valueOf(size));

            // result to hex string
            return result.longValue();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return -1;
    }

    public void printRing() {
        for (Long key : ring.keySet()) {
            System.out.println(key + " " + ring.get(key));
        }
    }
}
