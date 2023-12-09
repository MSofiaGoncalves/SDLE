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

    public AddWins() {
        this.id = null;
        this.cc = new HashSet<>();
        this.set = new HashSet<>();
        this.local_counter = 1;
    }

    public AddWins(String id) {
        this.id = id;
        this.cc = new HashSet<>();
        this.set = new HashSet<>();
        this.local_counter = 1;
    }

    public String getId(){
        return id;
    }

    public HashSet<Tuple<String, Long>> getCc(){
        return cc;
    }

    public HashSet<Triple<String, String, Long>> getSet(){
        return set;
    }

    public long getLocalCounter(){
        return local_counter;
    }

    public String setId(String id){
        return this.id = id;
    }

    public HashSet<Tuple<String, Long>> setCc(HashSet<Tuple<String, Long>> cc){
        return this.cc = cc;
    }

    public HashSet<Triple<String, String, Long>> setSet(HashSet<Triple<String, String, Long>> set){
        return this.set = set;
    }

    public long setLocalCounter(long local_counter){
        return this.local_counter = local_counter;
    }

    public void showSet(){
        for(Triple<String, String, Long> triple : this.set){
            System.out.println(triple.getFirst() + " " + triple.getSecond() + " " + triple.getThird());
        }
    }

    public Boolean containsProduct(String p){
        for(Triple<String, String, Long> triple : this.set){
            if(triple.getSecond().equals(p)){
                return true;
            }
        }
        return false;
    }

    @Override
    public String toString() {
        StringBuilder result = new StringBuilder();
        for (Triple<String, String, Long> triple : this.set) {
            result.append(triple.getFirst())
                    .append(" ")
                    .append(triple.getSecond())
                    .append(" ")
                    .append(triple.getThird())
                    .append("\n");
        }
        return result.toString();
    }

    public void showCc(){
        for(Tuple<String, Long> tuple : this.cc){
            System.out.println(tuple.getFirst() + " " + tuple.getSecond());
        }
    }

    public HashSet<String> elements() {
        HashSet<String> elements = new HashSet<>();
        for (Triple<String, String, Long> triple : this.set) {
            elements.add(triple.getSecond());
        }
        return elements;
    }

    public void add(String element, String username) {
        this.cc.add(new Tuple<>(username, this.local_counter));
        this.set.add(new Triple<>(username, element, this.local_counter));
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
