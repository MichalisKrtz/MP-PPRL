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
import java.util.stream.Stream;

public class MetricSpaceProtocol implements PPRLProtocol {
    private final List<Party> parties;
    MetricSpace metricSpace;
    private final double maximalIntersection;
    private final double similarityThreshold;

    public MetricSpaceProtocol(List<Party> parties, double maximalIntersection, double similarityThreshold) {
        this.parties = parties;
        this.maximalIntersection = maximalIntersection;
        this.similarityThreshold = similarityThreshold;
        this.metricSpace = new MetricSpace();
    }

    public void execute() {
        Indexer indexer = new Indexer(metricSpace);

        Set<Cluster> firstDSClusters = convertRecordsToSingletonClusters(parties.getFirst().getBloomFilterEncodedRecords());

        // INDEXING (first dataset)
        indexer.selectFarAwayPivots(firstDSClusters, 50);
        indexer.assignElementsToPivots(firstDSClusters, maximalIntersection);

        for (int i = 1; i < parties.size(); i++) {
            // LINKING
            Set<Edge> edges = new HashSet<>();
            Set<Cluster> qClusters = convertRecordsToSingletonClusters(parties.get(i).getBloomFilterEncodedRecords());
            // Iterate query records
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
            indexer.assignElementsToPivots(qClusters, maximalIntersection);
        }
    }

    public Set<RecordIdentifierCluster> getResults() {
        Set<RecordIdentifierCluster> results = new HashSet<>();

        // Iterate over each entry in the map
        for (Map.Entry<Pivot, List<Cluster>> entry : metricSpace.pivotElementsMap.entrySet()) {
            Pivot pivot = entry.getKey();
            List<Cluster> clusters = entry.getValue();

            // Process the Pivot's own cluster
            Cluster pivotCluster = pivot.getCluster();  // Assuming Pivot has a getCluster() method
            results.add(convertClusterToRecordIdentifierCluster(pivotCluster));

            // Process each cluster in the List<Cluster>
            for (Cluster cluster : clusters) {
                results.add(convertClusterToRecordIdentifierCluster(cluster));
            }
        }

        return results;
    }

    // Helper method to convert a Cluster to a RecordIdentifierCluster
    private RecordIdentifierCluster convertClusterToRecordIdentifierCluster(Cluster cluster) {
        Set<RecordIdentifier> recordIdentifiers = cluster.bloomFilterEncodedRecordsSet().stream()
                .map(encodedRecord -> new RecordIdentifier(encodedRecord.party(), encodedRecord.id()))
                .collect(Collectors.toSet());

        return new RecordIdentifierCluster(recordIdentifiers);
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

        return bitsSetToOne * ((1 - similarityThreshold) / similarityThreshold);
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
