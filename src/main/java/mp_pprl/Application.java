package mp_pprl;

import mp_pprl.protocols.EarlyMappingClusteringProtocol;
import mp_pprl.protocols.Party;
import mp_pprl.repositories.PartiesRepository;
import mp_pprl.graph.Vertex;
import mp_pprl.db.Record;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

public class Application {
    // Databases
    private final static String db_one_path = "C:\\MP-PPRL Databases\\dataset_one.db";
    private final static String db_two_path = "C:\\MP-PPRL Databases\\dataset_two.db";
    private final static String db_three_path = "C:\\MP-PPRL Databases\\dataset_three.db";

    public static void run() {
        // Parameters
        final int bloomFilterLength = 1000;
        final int numberOfHashFunctions = 30;
        final int minimumSubsetSize = 3;
        final double similarityThreshold = 0.85;

        final String[] privateFields = {"first_name", "last_name"};
        final String[] quasiIdentifiers = {"first_name", "last_name"};
        final String[] blockingKeyValues = {"first_name", "last_name"};

        // Timing
        long start, end, elapsedTime;
        start = System.currentTimeMillis();
        runEarlyMappingClusteringProtocol(bloomFilterLength, numberOfHashFunctions, minimumSubsetSize, similarityThreshold, privateFields, quasiIdentifiers, blockingKeyValues);
        end = System.currentTimeMillis();
        elapsedTime = end - start;
        if (elapsedTime < 1000) {
            System.out.println("Total Elapsed Time: " + elapsedTime + " milliseconds.");
        } else {
            System.out.println("Total Elapsed Time: " + TimeUnit.MILLISECONDS.toSeconds(elapsedTime) + " seconds.");
        }
    }


    public static void runEarlyMappingClusteringProtocol(int bloomFilterLength, int numberOfHashFunctions, int minimumSubsetSize, double similarityThreshold, String[] privateFields, String[] quasiIdentifiers, String[] blockingKeyValues) {
        System.out.println("Application running...");


        System.out.println("Getting records from the databases...");
        List<Record> partyOneRecords = PartiesRepository.selectAll(db_one_path);
        List<Record> partyTwoRecords = PartiesRepository.selectAll(db_two_path);
        List<Record> partyThreeRecords = PartiesRepository.selectAll(db_three_path);

        Party partyOne = new Party(privateFields, quasiIdentifiers, blockingKeyValues);
        Party partyTwo = new Party(privateFields, quasiIdentifiers, blockingKeyValues);
        Party partyThree = new Party(privateFields, quasiIdentifiers, blockingKeyValues);

        System.out.println("Adding records to parties...");
        partyOne.addRecords(partyOneRecords);
        partyTwo.addRecords(partyTwoRecords);
        partyThree.addRecords(partyThreeRecords);

        System.out.println("Parties sharing records...");
        Map<String, List<Record>> partyOneSharedRecords = partyOne.shareRecords(bloomFilterLength, numberOfHashFunctions);
        Map<String, List<Record>> partyTwoSharedRecords = partyTwo.shareRecords(bloomFilterLength, numberOfHashFunctions);
        Map<String, List<Record>> partyThreeSharedRecords = partyThree.shareRecords(bloomFilterLength, numberOfHashFunctions);

        List<Map<String, List<Record>>> sharedRecords = new ArrayList<>();
        sharedRecords.add(partyOneSharedRecords);
        sharedRecords.add(partyTwoSharedRecords);
        sharedRecords.add(partyThreeSharedRecords);

        System.out.println("Early mapping clustering protocol...");
        EarlyMappingClusteringProtocol EMap = new EarlyMappingClusteringProtocol(sharedRecords);
        Set<Vertex> clusters = EMap.generateClusters(similarityThreshold, minimumSubsetSize);

        System.out.println("Early Mapping Clustering Protocol finished successfully");

        System.out.println("Number of clusters: " + clusters.size());
        if (privateFields.length == 0) {
            printClusterRecords(clusters);
        }
    }

    public static void printClusterRecords(Set<Vertex> clusters) {
        for (Vertex cluster : clusters) {
            System.out.println("Cluster:");
            System.out.print("[");
            for (Record r : cluster.records()) {
                System.out.print(r.get("first_name").getValueAsString() + " " + r.get("last_name").getValueAsString() + ", ");
            }
            System.out.println("]");
        }
    }
}
