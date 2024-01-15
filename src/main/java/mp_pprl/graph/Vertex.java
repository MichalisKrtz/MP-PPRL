package mp_pprl.graph;

import mp_pprl.db.Record;

import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

public record Vertex(Set<Record> records) {
    public Vertex() {
        this(new HashSet<>());
    }

    public Vertex(Record record) {
        this(new HashSet<>(Collections.singletonList(record)));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Vertex vertex = (Vertex) o;
        return Objects.equals(records, vertex.records);
    }

    @Override
    public int hashCode() {
        return Objects.hash(records);
    }
}
