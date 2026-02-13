package mp_pprl.core.graph;

import mp_pprl.core.BloomFilterEncodedRecord;

import java.util.*;

public record Cluster(Set<BloomFilterEncodedRecord> bloomFilterEncodedRecordsSet) {
    public Cluster(BloomFilterEncodedRecord bloomFilterEncodedRecord) {
        this(new HashSet<>(Collections.singleton(bloomFilterEncodedRecord)));
    }

    public Cluster(List<BloomFilterEncodedRecord> records) {
        this(new HashSet<>(records));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Cluster cluster = (Cluster) o;
        return Objects.equals(bloomFilterEncodedRecordsSet, cluster.bloomFilterEncodedRecordsSet);
    }

    @Override
    public int hashCode() {
        return Objects.hash(bloomFilterEncodedRecordsSet);
    }
}

