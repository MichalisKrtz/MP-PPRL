package mp_pprl.dynamic_metric_space;

import mp_pprl.core.graph.Cluster;

import java.util.*;

public class Indexer {
    private int numberOfIndexedRecords = 0;
    private int intersections = 0;
    private final MetricSpace metricSpace;


    public Indexer(MetricSpace metricSpace) {
        this.metricSpace = metricSpace;
    }

    // Sets the first 2 initial pivots
    public void setInitialPivots(Set<Cluster> clusters) {
        Iterator<Cluster> iterator = clusters.iterator();
        Cluster cluster1 = iterator.next();
        Cluster cluster2 = iterator.next();

        double maxDistance = Double.MIN_VALUE;
        for (Cluster outerCluster : clusters) {
            for (Cluster innerCluster : clusters) {
                if (outerCluster.equals(innerCluster)) {
                    continue;
                }
                double dist = MetricSpace.distance(outerCluster, innerCluster);
                if (dist > maxDistance) {
                    maxDistance = dist;
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

    public void selectFarAwayPivots(Set<Cluster> clusters, int m) {
        if (clusters.isEmpty()) {
            return;
        }
        Set<Pivot> pivots = new HashSet<>();
        List<Cluster> clustersUsed = new ArrayList<>();

        // Make the first element a pivot
        Cluster x = clusters.iterator().next();
        pivots.add(new Pivot(x));
        clustersUsed.add(x);

        while (pivots.size() < m) {
            if (clustersUsed.size() == clusters.size()) {
                break;
            }

            Cluster bestCandidate = null;
            double maxDistanceSum = -1;
            for (Cluster candidate : clusters) {
                double candidateDistanceSum = 0;
                for (Pivot pivot : pivots) {
                    candidateDistanceSum += MetricSpace.distance(candidate, pivot.getCluster());
                }

                if (candidateDistanceSum > maxDistanceSum) {
                    maxDistanceSum = candidateDistanceSum;
                    bestCandidate = candidate;
                }
            }

            pivots.add(new Pivot(bestCandidate));
            clustersUsed.add(bestCandidate);
        }

        for (Cluster clusterUsed : clustersUsed) {
            clusters.remove(clusterUsed);
        }
        for (Pivot pivot : pivots) {
            metricSpace.pivotElementsMap.put(pivot, new ArrayList<>());
            metricSpace.pivotElementsDistanceMap.put(pivot, new ArrayList<>());
        }
    }

    public void assignElementsToPivots(Set<Cluster> dataset, double maximalIntersection) {
//        System.out.println("Number of indexed records: " + numberOfIndexedRecords);
//        System.out.println("Number of pivots: " + metricSpace.pivotElementsMap.size());

        for (Cluster cluster : dataset) {
            numberOfIndexedRecords++;
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
            if (p.equals(newPivot)) continue;
            Iterator<Cluster> iter = metricSpace.pivotElementsMap.get(p).iterator();
            while (iter.hasNext()) {
                Cluster cluster = iter.next();
                double newDistance = MetricSpace.distance(cluster, newPivot.getCluster());
                int elementIndex = metricSpace.pivotElementsMap.get(p).indexOf(cluster);
                double oldDistance =  metricSpace.pivotElementsDistanceMap.get(p).get(elementIndex);

                if (newDistance < oldDistance) {
                    metricSpace.pivotElementsMap.get(newPivot).add(cluster);
                    metricSpace.pivotElementsDistanceMap.get(newPivot).add(newDistance);
                    if (newPivot.getRadius() < newDistance) {
                        newPivot.setRadius(newDistance);
                    }

                    metricSpace.pivotElementsDistanceMap.get(p).remove(elementIndex);
                    iter.remove();
//                    metricSpace.pivotElementsMap.get(p).remove(elementIndex);
                    if (metricSpace.pivotElementsDistanceMap.get(p).isEmpty()) {
                        p.setRadius(0);
                    } else {
                        p.setRadius(Collections.max(metricSpace.pivotElementsDistanceMap.get(p)));
                    }

                }
            }
        }

//        //Recalculate intersections
//        long start, end;
//        start = System.currentTimeMillis();
//        intersections = 0;
//        List<Cluster> allClusters = new ArrayList<>();
//        for (List<Cluster> clusterList : metricSpace.pivotElementsMap.values()) {
//            allClusters.addAll(clusterList);
//        }
//        for (Pivot p : metricSpace.pivotElementsMap.keySet()) {
//            //Increment once because the pivot's record, intersects the pivot radius
//            intersections++;
//            for (Cluster cluster : allClusters) {
//                double dist = MetricSpace.distance(cluster, p.getCluster());
//                if (dist <= p.getRadius()) {
//                    intersections++;
//                }
//            }
//        }
//        end = System.currentTimeMillis();
//        elapsedTime += end - start;
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
        return (double) intersections / (numberOfIndexedRecords * metricSpace.pivotElementsMap.size());

    }

}
