package crdts.utils;

import java.util.Objects;

public class Triple<X, Y, Z> {

    public final X x;
    public final Y y;
    public final Z z;

    public Triple(X x, Y y, Z z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Triple<?, ?, ?> triple = (Triple<?, ?, ?>) obj;
        return Objects.equals(x, triple.x) && Objects.equals(y, triple.y) && Objects.equals(z, triple.z);
    }

    @Override
    public int hashCode() {
        return Objects.hash(x, y, z);
    }

    public X getFirst(){
        return x;
    }

    public Y getSecond(){
        return y;
    }

    public Z getThird(){
        return z;
    }

}