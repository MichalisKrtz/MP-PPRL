package mp_pprl.dynamic_metric_space;

import mp_pprl.PPRLProtocol;
import mp_pprl.RecordIdentifier;
import mp_pprl.RecordIdentifierCluster;
import mp_pprl.core.Party;
import mp_pprl.core.BloomFilterEncodedRecord;
import mp_pprl.core.graph.Cluster;
import mp_pprl.core.graph.Edge;
import mp_pprl.incremental_clustering.optimization.HungarianAlgorithm;

import java.util.*;
import java.util.stream.Collectors;

public class MetricSpaceProtocol implements PPRLProtocol {
    private final List<Party> parties;
    MetricSpace metricSpace;
    private static final double MAXIMAL_INTERSECTION = 0.003;
    private static final double SIMILARITY_THRESHOLD = 0.9;

    public MetricSpaceProtocol(List<Party> parties) {
        this.parties = parties;
        this.metricSpace = new MetricSpace();
    }

    public void execute() {
        Indexer indexer = new Indexer(metricSpace);

        Set<Cluster> firstDSClusters = convertRecordsToSingletonClusters(parties.getFirst().getBloomFilterEncodedRecords());

        // INDEXING (first dataset)
        indexer.selectFarAwayPivots(firstDSClusters, 40);
        indexer.assignElementsToPivots(firstDSClusters, MAXIMAL_INTERSECTION);

        for (int i = 1; i < parties.size(); i++) {
            // LINKING
            Set<Edge> edges = new HashSet<>();
            Set<Cluster> qClusters = convertRecordsToSingletonClusters(parties.get(i).getBloomFilterEncodedRecords());
            // Iterate query records
            long startTimeBig = System.currentTimeMillis();
            for (Cluster qSingletonCluster : qClusters) {
                double qRecordRadius = queryRecordRadius(qSingletonCluster.bloomFilterEncodedRecordsSet().iterator().next());
                // Iterate Pivots
                for (Pivot pivot : metricSpace.pivotElementsMap.keySet()) {
                    double pivotQRecordDistance = MetricSpace.distance(pivot.getCluster(), qSingletonCluster);
                    if (!queryRecordOverlapsWithPivot(pivot, pivotQRecordDistance, qRecordRadius)) {
                        continue;
                    }
                    // Check if the query record can be linked with the pivot's cluster
                    if (queryRecordSatisfiesTriangleInequality(pivotQRecordDistance, 0, qRecordRadius)) {
                        double distance = MetricSpace.distance(pivot.getCluster(), qSingletonCluster);
                        if (distance <= qRecordRadius) {
                            edges.add(new Edge(pivot.getCluster(), qSingletonCluster, distance));
                        }
                    }
                    // Check if the query record can be linked with any of the clusters assigned to the pivot
                    for (int j = 0; j < metricSpace.pivotElementsMap.get(pivot).size(); j++) {
                        double pivotClusterDistance = metricSpace.pivotElementsDistanceMap.get(pivot).get(j);
                        if (queryRecordSatisfiesTriangleInequality(pivotQRecordDistance, pivotClusterDistance, qRecordRadius)) {
                            double distance = MetricSpace.distance(metricSpace.pivotElementsMap.get(pivot).get(j), qSingletonCluster);
                            if (distance <= qRecordRadius) {
                                edges.add(new Edge(metricSpace.pivotElementsMap.get(pivot).get(j),
                                                qSingletonCluster,
                                                distance
                                        )
                                );
                            }
                        }
                    }
                }
            }

            // CLUSTERING
            Set<Edge> optimalEdges = new HashSet<>(HungarianAlgorithm.computeAssignments(edges, false));
            mergeQueryClusters(optimalEdges, qClusters);

            // INDEXING
            // Index the remaining query records that where not linked with any cluster
            indexer.assignElementsToPivots(qClusters, MAXIMAL_INTERSECTION);
        }
    }

    public Set<RecordIdentifierCluster> getResults() {
        return metricSpace.pivotElementsMap.values().stream()
                .flatMap(List::stream)
                .map(cluster -> cluster.bloomFilterEncodedRecordsSet().stream()
                        .map(encodeRecord -> new RecordIdentifier(encodeRecord.party(), encodeRecord.id()))
                        .collect(Collectors.toSet()))
                .map(RecordIdentifierCluster::new)
                .collect(Collectors.toSet());
    }

    // This method will work correctly only if the first cluster of the edges is either a pivot's cluster or a cluster
    // assigned to a pivot. The second cluster should be a singleton cluster, containing only a query record.
    private void mergeQueryClusters(Set<Edge> edges, Set<Cluster> queryClusters) {
        Iterator<Edge> iterator  = edges.iterator();
        while (iterator.hasNext()) {
            Edge e = iterator.next();
            for (BloomFilterEncodedRecord bloomFilterEncodedRecord : e.c2().bloomFilterEncodedRecordsSet()) {
                e.c1().bloomFilterEncodedRecordsSet().add(bloomFilterEncodedRecord);
            }
            queryClusters.remove(e.c2());
            iterator.remove();
        }
    }

    private Edge getQClusterUniqueEdge(Cluster qCluster, Set<Edge> edges) {
        for (Edge edge : edges) {
            if (edge.c2().equals(qCluster)) {
                return edge;
            }
        }
        return null;
    }

    private double queryRecordRadius(BloomFilterEncodedRecord record) {
        int bitsSetToOne = 0;
        for (byte cell : record.bloomFilter().getVector()) {
            bitsSetToOne += cell;
        }

        return bitsSetToOne * ((1 - SIMILARITY_THRESHOLD) / SIMILARITY_THRESHOLD);
    }

    private boolean queryRecordOverlapsWithPivot(Pivot pivot, double pivotQRecordDistance, double qRecordRadius) {
        return pivotQRecordDistance <= (pivot.getRadius() + qRecordRadius);
    }

    private boolean queryRecordSatisfiesTriangleInequality(double pivotQRecordDistance, double pivotClusterDistance, double qRecordRadius) {
        return Math.abs(pivotQRecordDistance - pivotClusterDistance) <= qRecordRadius;
    }

    private Set<Cluster> convertRecordsToSingletonClusters(List<BloomFilterEncodedRecord> bloomFilterEncodedRecords) {
        Set<Cluster> clusters = new HashSet<>();
        for (BloomFilterEncodedRecord record : bloomFilterEncodedRecords) {
            clusters.add(new Cluster(record));
        }

        return clusters;
    }

}
