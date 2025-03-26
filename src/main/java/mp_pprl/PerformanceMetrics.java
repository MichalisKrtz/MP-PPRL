package mp_pprl;

import java.util.Iterator;
import java.util.Set;

public class PerformanceMetrics {
    private final PPRLProtocol protocol;
    private Set<RecordIdentifierCluster> clusters;
    private final int numberOfParties;
    private long runTime;
    private final int positives;
    private int truePositives;
    private int falsePositives;
    private int falseNegatives;

    public PerformanceMetrics(PPRLProtocol protocol, int numberOfParties,int datasetSize, double matchingPercentage) {
        this.protocol = protocol;
        this.numberOfParties = numberOfParties;
        this.positives = (int) (datasetSize * matchingPercentage);
    }

    public void run() {
        long startTime, endTime;

        startTime = System.currentTimeMillis();
        protocol.execute();
        endTime = System.currentTimeMillis();

        runTime = endTime - startTime;
        clusters = protocol.getResults();
        calculateConfusionMatrix();
    }

    public long getRunTime() {
        return runTime;
    }

    public double calculatePrecision() {
        return (double) truePositives / (truePositives + falsePositives);
    }

    public double calculateRecall() {
        return (double) truePositives / (truePositives + falseNegatives);
    }

    public double calculateF1() {
        double precision = calculatePrecision();
        double recall = calculateRecall();
        return (double) 2 * ((precision * recall) / (precision + recall));
    }

    public void printClusters() {
        for (RecordIdentifierCluster cluster : clusters) {
            Iterator<RecordIdentifier> iterator = cluster.recordIdentifiers().iterator();
            while (iterator.hasNext()) {
                RecordIdentifier recordIdentifier = iterator.next();
                System.out.print(recordIdentifier.id());
                if (!iterator.hasNext()) {
                    System.out.println();
                    break;
                }
                System.out.print(" | ");
            }
        }
    }

    private void calculateConfusionMatrix() {
        truePositives = 0;
        falsePositives = 0;
        for (RecordIdentifierCluster cluster : clusters) {
            if (cluster.recordIdentifiers().size() < numberOfParties) {
                continue;
            }
            Iterator<RecordIdentifier> iter = cluster.recordIdentifiers().iterator();
            RecordIdentifier firstRecordIdentifier = iter.next();
            int count = 1;
            while (iter.hasNext()) {
                RecordIdentifier currentRecordIdentifier = iter.next();
                if (!firstRecordIdentifier.id().equals(currentRecordIdentifier.id())) {
                    falsePositives++;
                    break;
                }
                count++;
            }
            if (count == numberOfParties) {
                truePositives++;
            }
        }
        falseNegatives = positives - truePositives;
//        System.out.println("Positives: " + positives);
//        System.out.println("TruePositives: " + truePositives);
//        System.out.println("FalsePositives: " + falsePositives);
//        System.out.println("FalseNegatives: " + falseNegatives);
    }
}
