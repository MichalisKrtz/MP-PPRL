package mp_pprl.core.graph;

import mp_pprl.core.BloomFilterEncodedRecord;

import java.util.*;

public class WeightedGraph {
    private final Set<Cluster> clusters;
    private final Set<Edge> edges;

    public WeightedGraph() {
        clusters = new HashSet<>();
        edges = new HashSet<>();
    }

    public void clearEdges() {
        edges.clear();
    }

    public void mergeClusters() {
        Iterator<Edge> iterator  = edges.iterator();
        while (iterator.hasNext()) {
            Edge e = iterator.next();
            for (BloomFilterEncodedRecord bloomFilterEncodedRecord : e.c2().bloomFilterEncodedRecordsSet()) {
                e.c1().bloomFilterEncodedRecordsSet().add(bloomFilterEncodedRecord);
            }
            clusters.remove(e.c2());
            iterator.remove();
        }
    }

    public void addCluster(Cluster cluster) {
        clusters.add(cluster);
    }

    public void addClusters(Set<Cluster> clusters) {
        this.clusters.addAll(clusters);
    }

    public void addEdge(Cluster c1, Cluster c2, double similarity) {
        edges.add(new Edge(c1, c2, similarity));
    }

    public Set<Cluster> getClusters() {
        return clusters;
    }

    public Set<Edge> getEdges() {
        return edges;
    }
}
