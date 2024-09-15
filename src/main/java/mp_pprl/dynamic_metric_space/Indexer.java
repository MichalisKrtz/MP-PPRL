package mp_pprl.dynamic_metric_space;

import mp_pprl.core.domain.RecordIdentifier;
import mp_pprl.core.graph.Cluster;

import java.util.*;

public class Indexer {
    private int numberOfReadRecords = 0;
    private int intersections = 0;
    private final MetricSpace metricSpace;

    public Indexer(MetricSpace metricSpace) {
        this.metricSpace = metricSpace;
    }

    // Sets the first 2 initial pivots
    //TODO initialize more pivots
    public void setInitialPivots(Set<Cluster> clusters) {
        Cluster cluster1 = null;
        Cluster cluster2 = null;

        double maxDistance = Double.MIN_VALUE;
        for (Cluster outerCluster : clusters) {
            for (Cluster innerCluster : clusters) {
                if (outerCluster.equals(innerCluster)) {
                    continue;
                }
                double dist = MetricSpace.distance(outerCluster, innerCluster);
                if (dist > maxDistance) {
//                    System.out.println("Record Identifiers: " + outerCluster.recordIdentifiersSet().iterator().next().getId() + " - " + innerCluster.recordIdentifiersSet().iterator().next().getId());
                    maxDistance = dist;
//                    System.out.println("New max distance: " + maxDistance);
                    cluster1 = outerCluster;
                    cluster2 = innerCluster;
                }
            }
        }

        Pivot p1 = new Pivot(cluster1);
        Pivot p2 = new Pivot(cluster2);
        metricSpace.pivotElementsMap.put(p1, new ArrayList<>());
        metricSpace.pivotElementsMap.put(p2, new ArrayList<>());
        metricSpace.pivotElementsDistanceMap.put(p1, new ArrayList<>());
        metricSpace.pivotElementsDistanceMap.put(p2, new ArrayList<>());
        clusters.remove(cluster1);
        clusters.remove(cluster2);
    }

    public void assignElementsToPivots(Set<Cluster> dataset, double maximalIntersection) {
        for (Cluster cluster : dataset) {
            numberOfReadRecords++;
            double minDist = Double.MAX_VALUE;
            Pivot bestPivot = null;

            for (Pivot p : metricSpace.pivotElementsMap.keySet()) {
                double dist = MetricSpace.distance(cluster, p.getCluster());
                if (dist <= p.getRadius()) {
                    intersections++;
                }
                if (dist < minDist) {
                    minDist = dist;
                    bestPivot = p;
                }
            }

            if (bestPivot == null) {
                continue;
            }

            metricSpace.pivotElementsMap.get(bestPivot).add(cluster);
            metricSpace.pivotElementsDistanceMap.get(bestPivot).add(minDist);

            if (bestPivot.getRadius() < minDist) {
                bestPivot.setRadius(minDist);
            }
//            System.out.println("Overlap: " + overlap());
            if (overlap() > maximalIntersection) {
                generateNewPivot();
            }
        }
    }

    private void generateNewPivot() {
        Pivot maxCardPivot = maxCardinalityPivot();

        // Choose the furthest element from the maxCardinalityPivot
        double maxDist = metricSpace.pivotElementsDistanceMap.get(maxCardPivot).getFirst();
        Cluster maxDistElement = metricSpace.pivotElementsMap.get(maxCardPivot).getFirst();
        int maxDistElementIndex = 0;
        for (int i = 1; i < metricSpace.pivotElementsMap.get(maxCardPivot).size(); i++) {
            if (maxDist < metricSpace.pivotElementsDistanceMap.get(maxCardPivot).get(i)) {
                maxDist = metricSpace.pivotElementsDistanceMap.get(maxCardPivot).get(i);
                maxDistElement = metricSpace.pivotElementsMap.get(maxCardPivot).get(i);
                maxDistElementIndex = i;
            }
        }
        Pivot newPivot = new Pivot(maxDistElement);
        metricSpace.pivotElementsMap.put(newPivot, new ArrayList<>());
        metricSpace.pivotElementsMap.get(maxCardPivot).remove(maxDistElementIndex);
        metricSpace.pivotElementsDistanceMap.put(newPivot, new ArrayList<>());
        metricSpace.pivotElementsDistanceMap.get(maxCardPivot).remove(maxDistElementIndex);

        for (Pivot p : metricSpace.pivotElementsMap.keySet()) {
            Iterator<Cluster> iter = metricSpace.pivotElementsMap.get(p).iterator();
            while (iter.hasNext()) {
                Cluster cluster = iter.next();
                if (MetricSpace.distance(cluster, newPivot.getCluster()) < MetricSpace.distance(cluster, p.getCluster())) {
                    metricSpace.pivotElementsMap.get(newPivot).add(cluster);
                    metricSpace.pivotElementsDistanceMap.get(newPivot).add(MetricSpace.distance(cluster, newPivot.getCluster()));
                    if (newPivot.getRadius() < metricSpace.pivotElementsDistanceMap.get(newPivot).getLast()) {
                        newPivot.setRadius(metricSpace.pivotElementsDistanceMap.get(newPivot).getLast());
                    }

                    int elementIndex = metricSpace.pivotElementsMap.get(p).indexOf(cluster);
                    double oldDist =  metricSpace.pivotElementsDistanceMap.get(p).get(elementIndex);
                    metricSpace.pivotElementsDistanceMap.get(p).remove(elementIndex);
                    iter.remove();
//                    metricSpace.pivotElementsMap.get(p).remove(elementIndex);
                    if (metricSpace.pivotElementsDistanceMap.get(p).isEmpty()) {
                        p.setRadius(0);
                    } else if (p.getRadius() == oldDist) {
                        p.setRadius(Collections.max(metricSpace.pivotElementsDistanceMap.get(p)));
                    }

                }


            }
        }

        intersections = 0;
        List<Cluster> allClusters = new ArrayList<>();
        for (List<Cluster> clusterList : metricSpace.pivotElementsMap.values()) {
            allClusters.addAll(clusterList);
        }
        for (Pivot p : metricSpace.pivotElementsMap.keySet()) {
            intersections++;
            for (Cluster cluster : allClusters) {
                double dist = MetricSpace.distance(cluster, p.getCluster());
                if (dist <= p.getRadius()) {
                    intersections++;
                }
            }
        }

    }

    private Pivot maxCardinalityPivot() {
        Iterator<Pivot> iter = metricSpace.pivotElementsMap.keySet().iterator();
        Pivot maxCardPivot = iter.next();
        int maxCardinality = metricSpace.pivotElementsMap.get(maxCardPivot).size();

        while (iter.hasNext()) {
            Pivot p = iter.next();
            if (metricSpace.pivotElementsMap.get(p).size() > maxCardinality) {
                maxCardinality = metricSpace.pivotElementsMap.get(p).size();
                maxCardPivot = p;
            }
        }

        return maxCardPivot;
    }

    private double overlap() {
        return (double) intersections / (numberOfReadRecords * metricSpace.pivotElementsMap.size());

    }

}
