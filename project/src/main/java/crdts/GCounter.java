package crdts;

import java.util.HashMap;
import java.util.Map;

public class GCounter {
    private final String id;
    private final Map<String, Integer> counters;

    public GCounter(String id) {
        this.id = id;
        this.counters = new HashMap<>();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("GCounter{id='").append(id).append('\'');
        sb.append(", counters=").append(counters);
        sb.append('}');
        return sb.toString();
    }

    public String getId(){
        return id;
    }

    public Map<String, Integer> getCounters(){
        return counters;
    }

    public void increment(Integer value) {
        counters.put(id, counters.getOrDefault(id, 0) + value);
    }

    public int value() {
        return counters.values().stream().mapToInt(Integer::intValue).sum();
    }

    public void merge(GCounter other) {
        for (Map.Entry<String, Integer> entry : other.counters.entrySet()) {
            String otherId = entry.getKey();
            int otherCounter = entry.getValue();
            counters.put(otherId, Math.max(otherCounter, counters.getOrDefault(otherId, 0)));
        }
    }
}