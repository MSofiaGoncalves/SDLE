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

    public String getId(){
        return id;
    }



    public GCounter getInc(){
        return inc;
    }



    public GCounter getDec(){
        return dec;
    }



    public void increment(Integer value) {
        inc.increment(value);
    }

    public void decrement(Integer value) {
        dec.increment(value);
    }

    public int value() {
        return inc.value() - dec.value();
    }

    public void merge(PNCounter other) {
        inc.merge(other.inc);
        dec.merge(other.dec);
    }

}
