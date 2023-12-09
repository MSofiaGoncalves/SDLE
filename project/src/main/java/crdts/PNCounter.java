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

    public PNCounter(String id, GCounter inc, GCounter dec) {
        this.id = id;
        this.inc = inc;
        this.dec = dec;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("PNCounter{id='").append(id).append('\'');
        sb.append(", inc=").append(inc);
        sb.append(", dec=").append(dec);
        sb.append('}');
        return sb.toString();
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PNCounter pnCounter)) return false;
        return id.equals(pnCounter.id) &&
                inc.equals(pnCounter.inc) &&
                dec.equals(pnCounter.dec);
    }
}
