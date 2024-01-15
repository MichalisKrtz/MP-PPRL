package mp_pprl.graph;

import mp_pprl.db.Record;

import java.util.Objects;

public record Edge(Vertex vertex, Record record) {
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Edge edge = (Edge) o;

        if (!Objects.equals(vertex, edge.vertex)) return false;
        return Objects.equals(record, edge.record);
    }

    @Override
    public int hashCode() {
        int result = vertex != null ? vertex.hashCode() : 0;
        result = 31 * result + (record != null ? record.hashCode() : 0);
        return result;
    }
}
