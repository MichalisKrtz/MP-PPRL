package mp_pprl.dynamic_metric_space;

import mp_pprl.core.Party;
import mp_pprl.core.domain.RecordIdentifier;
import mp_pprl.core.graph.Cluster;
import mp_pprl.core.graph.Edge;
import mp_pprl.incremental_clustering.SimilarityCalculator;
import mp_pprl.incremental_clustering.optimization.Hungarian;
import mp_pprl.incremental_clustering.optimization.HungarianAlgorithm;

import java.util.*;

public class MetricSpaceProtocol {
    private final List<Party> parties;
    private static final double MAXIMAL_INTERSECTION = 0.001;
    private static final double SIMILARITY_THRESHOLD = 0.75;
    public MetricSpaceProtocol(List<Party> parties) {
        this.parties = parties;
    }

    public void run() {
        int skips1 = 0;
        int skips2 = 0;

        MetricSpace metricSpace = new MetricSpace();
        Indexer indexer = new Indexer(metricSpace);
        List<RecordIdentifier> firstDSRecordIdentifiers = parties.getFirst().getRecordIdentifiers();
        Set<Cluster> firstDSClusters = new HashSet<>();
        for (RecordIdentifier recordIdentifier : firstDSRecordIdentifiers) {
            firstDSClusters.add(new Cluster(recordIdentifier));
        }
        // INDEXING (first dataset)
        indexer.setInitialPivots(firstDSClusters);
        indexer.assignElementsToPivots(firstDSClusters, MAXIMAL_INTERSECTION);

        for (int i = 1; i < parties.size(); i++) {
            // LINKING
            Set<Edge> edges = new HashSet<>();
            Set<Cluster> qClusters = convertRecordsToSingletonClusters(parties.get(i).getRecordIdentifiers());
            // Iterate query records
            for (Cluster qSingletonCluster : qClusters) {
                double qRecordRadius = queryRecordRadius(qSingletonCluster.recordIdentifiersSet().iterator().next());
                // Iterate Pivots
                for (Pivot pivot : metricSpace.pivotElementsMap.keySet()) {
                    double pivotQRecordDistance = MetricSpace.distance(pivot.getCluster(), qSingletonCluster);
//                        double pivotQRecordDistance = SimilarityCalculator.averageSimilarity(pivot.getCluster(), qSingletonCluster.recordIdentifiersSet().iterator().next());
                    if (!queryRecordOverlapsWithPivot(pivot, pivotQRecordDistance, qRecordRadius)) {
                        skips1++;
                        continue;
                    }

                    // Check if the query record can be linked with the pivot's cluster
                    if (queryRecordSatisfiesTriangleInequality(pivotQRecordDistance, 0, qRecordRadius)) {
                        edges.add(new Edge(pivot.getCluster(), qSingletonCluster, pivotQRecordDistance));
                    } else {
                        skips2++;
                    }

                    // Check if the query record can be linked with any of the clusters assigned to the pivot
                    for (int j = 0; j < metricSpace.pivotElementsMap.get(pivot).size(); j++) {
                        double pivotClusterDistance = metricSpace.pivotElementsDistanceMap.get(pivot).get(j);
                        pivotQRecordDistance = MetricSpace.distance(metricSpace.pivotElementsMap.get(pivot).get(j), qSingletonCluster);
//                        pivotQRecordDistance = SimilarityCalculator.averageSimilarity(metricSpace.pivotElementsMap.get(pivot).get(j), qSingletonCluster.recordIdentifiersSet().iterator().next());
                        if (queryRecordSatisfiesTriangleInequality(pivotQRecordDistance, pivotClusterDistance, qRecordRadius)) {
                            edges.add(new Edge(metricSpace.pivotElementsMap.get(pivot).get(j), qSingletonCluster, pivotQRecordDistance));
                        } else {
                            skips2++;
                        }


                    }
                }
            }

//            System.out.println("Before Hungarian");
//            for (Edge edge : edges) {
//                System.out.println("Edge: " + edge.c1().recordIdentifiersSet().iterator().next().getId() + " - " + edge.c2().recordIdentifiersSet().iterator().next().getId() + ": " + edge.metric());
//            }
//            Set<Edge> optimalEdges = HungarianAlgorithm.computeAssignments(edges, false);
//            System.out.println("After Hungarian");
//            for (Edge oEdge : optimalEdges) {
//                System.out.println("Edge: " + oEdge.c1().recordIdentifiersSet().iterator().next().getId() + " - " + oEdge.c2().recordIdentifiersSet().iterator().next().getId());
//            }

//            linkQueryClusters(optimalEdges, qClusters);

            linkQueryClusters(edges, qClusters);
            // INDEXING
            // Index the remaining query records that where not linked with any cluster
            indexer.assignElementsToPivots(qClusters, MAXIMAL_INTERSECTION);
        }
        metricSpace.printMetricSpace();
//        metricSpace.printClusters();
        System.out.println("Total number of skips = " + skips1 + " + " + skips2);
    }

// This method will work correctly only if the first cluster of the edges is either a pivot's cluster or a cluster
//  assigned to a pivot. The second cluster should be a singleton cluster, containing only a query record.
    public void linkQueryClusters(Set<Edge> edges, Set<Cluster> queryClusters) {
        Iterator<Edge> iterator  = edges.iterator();
        while (iterator.hasNext()) {
            Edge e = iterator.next();
            for (RecordIdentifier recordIdentifier : e.c2().recordIdentifiersSet()) {
                e.c1().recordIdentifiersSet().add(recordIdentifier);
            }
            queryClusters.remove(e.c2());
            iterator.remove();
        }
    }

    public double queryRecordRadius(RecordIdentifier record) {
        int bitsSetToOne = 0;
        for (Byte bit : record.getBloomFilter().getVector()) {
            bitsSetToOne += bit;
        }

        return bitsSetToOne * ((1 - SIMILARITY_THRESHOLD) / SIMILARITY_THRESHOLD);
    }

    public boolean queryRecordOverlapsWithPivot(Pivot pivot, double pivotQRecordDistance, double qRecordRadius) {
        return pivotQRecordDistance <= (pivot.getRadius() + qRecordRadius);
    }

    public boolean queryRecordSatisfiesTriangleInequality(double pivotQRecordDistance, double pivotClusterDistance, double qRecordRadius) {
        return Math.abs(pivotQRecordDistance - pivotClusterDistance) <= qRecordRadius;
    }

    public Set<Cluster> convertRecordsToSingletonClusters(List<RecordIdentifier> recordIdentifiers) {
        Set<Cluster> clusters = new HashSet<>();
        for (RecordIdentifier record : recordIdentifiers) {
            clusters.add(new Cluster(record));
        }

        return clusters;
    }

}
