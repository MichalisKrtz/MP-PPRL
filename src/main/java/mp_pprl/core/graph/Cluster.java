package mp_pprl.core.graph;

import mp_pprl.core.domain.RecordIdentifier;

import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

public record Cluster(Set<RecordIdentifier> recordIdentifiersSet) {
    public Cluster(RecordIdentifier recordIdentifier) {
        this(new HashSet<>(Collections.singleton(recordIdentifier)));
    }
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Cluster cluster = (Cluster) o;
        return Objects.equals(recordIdentifiersSet, cluster.recordIdentifiersSet);
    }

    @Override
    public int hashCode() {
        return Objects.hash(recordIdentifiersSet);
    }
}

