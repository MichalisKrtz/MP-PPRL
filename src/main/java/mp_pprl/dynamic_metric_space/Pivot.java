package mp_pprl.dynamic_metric_space;

import mp_pprl.core.graph.Cluster;

public class Pivot {
    private final Cluster cluster;
    private float radius = 0;

    public Pivot(Cluster cluster) {
        this.cluster = cluster;
    }

    public Cluster getCluster() {
        return cluster;
    }

    public float getRadius() {
        return radius;
    }

    public void setRadius(float radius) {
        this.radius = radius;
    }
}
