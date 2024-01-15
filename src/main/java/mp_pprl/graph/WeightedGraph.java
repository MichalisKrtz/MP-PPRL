package mp_pprl.graph;

import mp_pprl.db.Record;

import java.util.HashSet;
import java.util.Set;

public class WeightedGraph {
    private final Set<Vertex> vertices;
    private final Set<Edge> edges;

    public WeightedGraph() {
        vertices = new HashSet<>();
        edges = new HashSet<>();
    }

    public void mergeClusterVertices(Edge e) {
        for (Vertex v : vertices) {
            if (e.vertex().equals(v)) {
                v.records().add(e.record());
//                System.out.println("Merged Edge!!!");
            } else {
//                System.out.println("Didn't merge edge.");
            }
        }
    }

    public void removeEdge(Edge e) {
        edges.remove(e);
    }

    public void addVertex(Vertex v) {
        vertices.add(v);
    }

    public void addVertices(Set<Vertex> vertices) {
        this.vertices.addAll(vertices);
    }

    public void addEdge(Vertex v, Record r) {
        edges.add(new Edge(v, r));
    }

    public Set<Vertex> getVertices() {
        return vertices;
    }

    public Set<Edge> getEdges() {
        return edges;
    }
}
