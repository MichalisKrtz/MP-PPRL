package mp_pprl.core.graph;

import java.util.Objects;

public record Edge(Cluster c1, Cluster c2, double metric) {
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Edge edge = (Edge) o;
        return Double.compare(metric, edge.metric) == 0 && Objects.equals(c1, edge.c1) && Objects.equals(c2, edge.c2);
    }

    @Override
    public int hashCode() {
        return Objects.hash(c1, c2, metric);
    }
}
