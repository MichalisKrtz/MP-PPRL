package mp_pprl;

import mp_pprl.core.data.SQLiteRecordRepository;
import mp_pprl.core.domain.Record;
import mp_pprl.core.domain.RecordIdentifier;
import mp_pprl.core.encoding.EncodingHandler;
import mp_pprl.incremental_clustering.EarlyMappingClusteringProtocol;
import mp_pprl.dynamic_metric_space.MetricSpaceProtocol;
import mp_pprl.core.Party;
import mp_pprl.core.domain.RecordRepository;
import mp_pprl.core.graph.Cluster;

import java.util.*;
import java.util.concurrent.TimeUnit;

public class Application {
    // Parameters
    private static final int bloomFilterLength = 1000;
    private static final int numberOfHashFunctions = 30;
    private static final String[] quasiIdentifiers = {"first_name", "last_name"};
    private static final String[] blockingKeyValues = {"first_name", "last_name"};
    private static final int minimumSubsetSize = 1;
    private static final double similarityThreshold = 0.75;
    // Databases
//    private final static String[] dbPaths = {
//            "C:\\MP-PPRL Databases\\dataset_one.db",
//            "C:\\MP-PPRL Databases\\dataset_one.db",
//            "C:\\MP-PPRL Databases\\dataset_one.db",
//            "C:\\MP-PPRL Databases\\dataset_one.db",
//            "C:\\MP-PPRL Databases\\dataset_one.db",
//            "C:\\MP-PPRL Databases\\dataset_one.db",
//            "C:\\MP-PPRL Databases\\dataset_one.db"};

    private final static String[] dbPaths = {
            "/home/michalis/MP-PPRL Databases/dataset_one.db",
            "/home/michalis/MP-PPRL Databases/dataset_one.db",
            "/home/michalis/MP-PPRL Databases/dataset_one.db"
    };

    public static void run() {
        // Timing
        long start, end, elapsedTime;
        start = System.currentTimeMillis();
//        runEarlyMappingClusteringProtocol(false);
        runMetricSpaceProtocol();
        end = System.currentTimeMillis();
        elapsedTime = end - start;
        if (elapsedTime < 1000) {
            System.out.println("Total Elapsed Time: " + elapsedTime + " milliseconds.");
        } else {
            System.out.println("Total Elapsed Time: " + TimeUnit.MILLISECONDS.toSeconds(elapsedTime) + " seconds.");
        }
    }

    public static void runMetricSpaceProtocol() {
        System.out.println("Application running...");


        System.out.println("Retrieving records from the databases...");
        int numberOfParties = dbPaths.length;
        List<RecordRepository> recordRepositories = new ArrayList<>();
        List<List<Record>> listsOfPartyRecords = new ArrayList<>();
        for (int i = 0; i < numberOfParties; i++) {
            recordRepositories.add(new SQLiteRecordRepository(dbPaths[i]));
            listsOfPartyRecords.add(recordRepositories.get(i).getAll());
        }

        System.out.println("Adding records to parties...");
        List<Party> parties = new ArrayList<>();
        for (int i = 0; i < numberOfParties; i++ ) {
            parties.add(new Party(quasiIdentifiers, blockingKeyValues, bloomFilterLength, numberOfHashFunctions));
            parties.get(i).addRecords(listsOfPartyRecords.get(i));
        }

        System.out.println("Encoding party records...");
        EncodingHandler encodingHandler = new EncodingHandler();
        for (Party party : parties) {
            party.encodeRecords(encodingHandler);
        }

        System.out.println("Metric space protocol...");
        MetricSpaceProtocol metricSpaceProtocol = new MetricSpaceProtocol(parties);
        metricSpaceProtocol.run();
    }

    public static void runEarlyMappingClusteringProtocol(boolean enhancedPrivacy) {
        System.out.println("Application running...");


        System.out.println("Retrieving records from the databases...");
        int numberOfParties = dbPaths.length;
        List<RecordRepository> recordRepositories = new ArrayList<>();
        List<List<Record>> listsOfPartyRecords = new ArrayList<>();
        for (int i = 0; i < numberOfParties; i++) {
            recordRepositories.add(new SQLiteRecordRepository(dbPaths[i]));
            listsOfPartyRecords.add(recordRepositories.get(i).getAll());
        }

        System.out.println("Adding records to parties...");
        List<Party> parties = new ArrayList<>();
        for (int i = 0; i < numberOfParties; i++ ) {
            parties.add(new Party(quasiIdentifiers, blockingKeyValues, bloomFilterLength, numberOfHashFunctions));
            parties.get(i).addRecords(listsOfPartyRecords.get(i));
        }

        System.out.println("Encoding party records...");
        EncodingHandler encodingHandler = new EncodingHandler();
        for (Party party : parties) {
            party.encodeRecords(encodingHandler);
        }

        System.out.println("Grouping party records...");
        for (Party party : parties) {
            party.generateRecordGroups();
        }

        System.out.println("Early mapping clustering protocol...");
        Set<String> unionOfBlocks = getUnionOfBlocks(parties);
        EarlyMappingClusteringProtocol clusteringProtocol = new EarlyMappingClusteringProtocol(parties, unionOfBlocks, similarityThreshold, minimumSubsetSize, bloomFilterLength);

        Set<Cluster> clusters = clusteringProtocol.execute(enhancedPrivacy);

        System.out.println("Early Mapping Clustering Protocol finished successfully");
        System.out.println("Number of clusters: " + clusters.size());

        for (Cluster c : clusters) {
            System.out.print("Cluster: " );
            for (RecordIdentifier r : c.recordIdentifiersSet()) {
                System.out.print(r.getId() + ", ");
            }
            System.out.println();
        }
    }

    private static Set<String> getUnionOfBlocks(List<Party> parties) {
        Set<String> blocks = new HashSet<>();
        for (Party party : parties) {
            blocks.addAll(party.getRecordIdentifierGroups().keySet());
        }

        return blocks;
    }

}
