package mp_pprl.graph;

import mp_pprl.domain.RecordIdentifier;

import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

public record Cluster(Set<RecordIdentifier> recordIdentifierList) {
    public Cluster(RecordIdentifier recordIdentifier) {
        this(new HashSet<>(Collections.singleton(recordIdentifier)));
    }
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Cluster cluster = (Cluster) o;
        return Objects.equals(recordIdentifierList, cluster.recordIdentifierList);
    }

    @Override
    public int hashCode() {
        return Objects.hash(recordIdentifierList);
    }
}
