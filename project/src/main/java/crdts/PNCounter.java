package crdts;


public class PNCounter {
    private final String id;
    private final GCounter inc;
    private final GCounter dec;

    public PNCounter(String id) {
        this.id = id;
        this.inc = new GCounter(id);
        this.dec = new GCounter(id);
    }

    public void increment() {
        inc.increment();
    }

    public void decrement() {
        dec.increment();
    }

    public int value() {
        return inc.value() - dec.value();
    }

    public void merge(PNCounter other) {
        inc.merge(other.inc);
        dec.merge(other.dec);
    }

}
