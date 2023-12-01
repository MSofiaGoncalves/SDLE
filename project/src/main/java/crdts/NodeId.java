package crdts;

import java.util.Objects;

public class NodeId {
    private long port;
    private String addr;

    public NodeId(long port, String addr) {
        this.port = port;
        this.addr = addr;
    }

    public long getPort() {
        return port;
    }

    public String getAddr() {
        return addr;
    }

    public int getBytesSize() {
        return Long.BYTES + addr.length();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        NodeId nodeId = (NodeId) o;
        return port == nodeId.port && Objects.equals(addr, nodeId.addr);
    }

    @Override
    public int hashCode() {
        return Objects.hash(port, addr);
    }

    @Override
    public String toString() {
        return addr + port;
    }
}