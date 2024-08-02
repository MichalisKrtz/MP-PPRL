package mp_pprl.dynamic_metric_space;

import mp_pprl.core.domain.RecordIdentifier;

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
    public void setInitialPivots(List<RecordIdentifier> elements) {
        RecordIdentifier element1 = null;
        RecordIdentifier element2 = null;

        int maxDistance = Integer.MIN_VALUE;

        for (int i = 0; i < elements.size(); i++) {
            for (int j = i + 1; j < elements.size(); j++) {
                int dist = MetricSpace.distance(elements.get(i), elements.get(j));
                if (dist > maxDistance) {
                    maxDistance = dist;
                    element1 = elements.get(i);
                    element2 = elements.get(j);
                }
            }
        }

        Pivot p1 = new Pivot(element1);
        Pivot p2 = new Pivot(element2);
        metricSpace.pivotElementsMap.put(p1, new ArrayList<>());
        metricSpace.pivotElementsMap.put(p2, new ArrayList<>());
        metricSpace.pivotElementsDistanceMap.put(p1, new ArrayList<>());
        metricSpace.pivotElementsDistanceMap.put(p2, new ArrayList<>());
        elements.remove(element1);
        elements.remove(element2);
    }

    public void assignElementsToPivots(List<RecordIdentifier> dataset, double maximalIntersection) {
        for (RecordIdentifier element : dataset) {
            numberOfReadRecords++;
            int minDist = Integer.MAX_VALUE;
            Pivot bestPivot = null;

            for (Pivot p : metricSpace.pivotElementsMap.keySet()) {
                int dist = MetricSpace.distance(element, p.getRecordIdentifier());
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

            metricSpace.pivotElementsMap.get(bestPivot).add(element);
            metricSpace.pivotElementsDistanceMap.get(bestPivot).add(minDist);

            if (bestPivot.getRadius() < minDist) {
                bestPivot.setRadius(minDist);
            }
            System.out.println("Overlap: " + overlap());
            if (overlap() > maximalIntersection) {
                generateNewPivot();
            }
        }
    }

    private void generateNewPivot() {
        Pivot maxCardPivot = maxCardinalityPivot();

        int maxDist = metricSpace.pivotElementsDistanceMap.get(maxCardPivot).getFirst();
        RecordIdentifier maxDistElement = metricSpace.pivotElementsMap.get(maxCardPivot).getFirst();
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
            Iterator<RecordIdentifier> iter = metricSpace.pivotElementsMap.get(p).iterator();
            while (iter.hasNext()) {
                RecordIdentifier element = iter.next();
                if (MetricSpace.distance(element, newPivot.getRecordIdentifier()) < MetricSpace.distance(element, p.getRecordIdentifier())) {
                    metricSpace.pivotElementsMap.get(newPivot).add(element);
                    metricSpace.pivotElementsDistanceMap.get(newPivot).add(MetricSpace.distance(element, newPivot.getRecordIdentifier()));
                    if (newPivot.getRadius() < metricSpace.pivotElementsDistanceMap.get(newPivot).getLast()) {
                        newPivot.setRadius(metricSpace.pivotElementsDistanceMap.get(newPivot).getLast());
                    }

                    int elementIndex = metricSpace.pivotElementsMap.get(p).indexOf(element);
                    int oldDist =  metricSpace.pivotElementsDistanceMap.get(p).get(elementIndex);
                    metricSpace.pivotElementsDistanceMap.get(p).remove(elementIndex);
                    iter.remove();
//                    metricSpace.pivotElementsMap.get(p).remove(elementIndex);
                    if (metricSpace.pivotElementsDistanceMap.get(p).isEmpty()) {
                        p.setRadius(Integer.MIN_VALUE);
                    } else if (p.getRadius() == oldDist) {
                        p.setRadius(Collections.max(metricSpace.pivotElementsDistanceMap.get(p)));
                    }

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
