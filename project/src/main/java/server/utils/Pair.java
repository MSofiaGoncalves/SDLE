package server.utils;

import com.google.gson.annotations.SerializedName;

public class Pair<K, V> {
    @SerializedName("quantity")
    public K first;
    @SerializedName("quantityBought")
    public V second;

    public Pair(K first, V second) {
        this.first = first;
        this.second = second;
    }

    @Override
    public String toString() {
        return "(" + first + ", " + second + ")";
    }
}
