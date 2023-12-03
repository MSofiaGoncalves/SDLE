package crdts;

import java.util.HashSet;
import java.util.Iterator;

import crdts.utils.Triple;
import crdts.utils.Tuple;

public class AddWins {
    private String id;
    private HashSet<Tuple<String, Long>> cc;
    private HashSet<Triple<String, String, Long>> set;
    private long local_counter;

    public AddWins(String id) {
        this.id = id;
        this.cc = new HashSet<>();
        this.set = new HashSet<>();
        this.local_counter = 1;
    }


    public HashSet<String> elements() {
        HashSet<String> elements = new HashSet<>();
        for (Triple<String, String, Long> triple : this.set) {
            elements.add(triple.getSecond());
        }
        return elements;
    }

    public void add(String element) {
        this.cc.add(new Tuple<>(this.id, this.local_counter));
        this.set.add(new Triple<>(this.id, element, this.local_counter));
        this.local_counter += 1;
    }

    public void rm(String element) {

        Iterator<Triple<String, String, Long>> iterator = this.set.iterator();
        while (iterator.hasNext()) {
            Triple<String, String, Long> triple = iterator.next();
            if (triple.getSecond().equals(element)) {
                iterator.remove();
            }
        }

        System.out.println(this);
    }

    public void join(AddWins other) {
        HashSet<Triple<String, String, Long>> newSet = new HashSet<>();

        for (Triple<String, String, Long> v : this.set) {
            if (other.set.contains(v) || !other.cc.contains(new Tuple<>(v.getFirst(), v.getThird()))) {
                newSet.add(v);
            }
        }

        for (Triple<String, String, Long> entry : other.set) {
            if (!this.cc.contains(new Tuple<>(entry.getFirst(), entry.getThird()))) {
                newSet.add(entry);
            }
        }

        this.set = newSet;
        this.cc.addAll(other.cc);
    }
}
