package mp_pprl;

import mp_pprl.core.data.SQLiteRecordRepository;
import mp_pprl.core.domain.Record;
import mp_pprl.core.BloomFilterEncodedRecord;
import mp_pprl.core.encoding.EncodingHandler;
import mp_pprl.incremental_clustering.EarlyMappingClusteringProtocol;
import mp_pprl.dynamic_metric_space.MetricSpaceProtocol;
import mp_pprl.core.Party;
import mp_pprl.core.domain.RecordRepository;
import mp_pprl.core.graph.Cluster;
import mp_pprl.soundex_based.SoundexBasedProtocol;

import java.util.*;
import java.util.concurrent.TimeUnit;

public class Application {
    // Parameters
    private static final int bloomFilterLength = 1000;
    private static final int numberOfHashFunctions = 30;
//    private static final String[] quasiIdentifiers = {"first_name", "last_name"};
    private static final String[] quasiIdentifiers = {"id", "first_name", "last_name", "middle_name", "address", "age"};
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
            "/home/michalis/Development/Thesis/Dev/MP-PPRL Databases/A_1600000.db",
            "/home/michalis/Development/Thesis/Dev/MP-PPRL Databases/B_1600000.db",
    };

    public static void run() {
        // Timing
        long start, end, elapsedTime;
        start = System.currentTimeMillis();
//        runEarlyMappingClusteringProtocol(false);
//        runMetricSpaceProtocol();
        runSoundexBasedProtocol();
        end = System.currentTimeMillis();
        elapsedTime = end - start;
        if (elapsedTime < 1000) {
            System.out.println("Total Elapsed Time: " + elapsedTime + " milliseconds.");
        } else {
            System.out.println("Total Elapsed Time: " + TimeUnit.MILLISECONDS.toSeconds(elapsedTime) + " seconds.");
        }
    }

    public static void runSoundexBasedProtocol() {
        System.out.println("Soundex Based Protocol running...");

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
        for (Party party : parties) {
            party.encodeRecordsWithSoundex();
        }

        System.out.println("Generating noise data...");
        for (Party party : parties) {
            party.generateNoise(0.1);
        }

        System.out.println("Truncate data...");
        for (Party party : parties) {
            party.truncateSoundexEncodedRecords();
        }

        System.out.println("Generating hashes...");
        EncodingHandler encodingHandler = new EncodingHandler();
        for (Party party : parties) {
            party.generateHashesForSoundexEncodedRecords(encodingHandler);
        }

        System.out.println("Soundex based protocol...");
        SoundexBasedProtocol soundexBasedProtocol = new SoundexBasedProtocol(parties);
        soundexBasedProtocol.run();
    }

    public static void runMetricSpaceProtocol() {
        System.out.println("Metric Space Protocol running...");


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
        System.out.println("Early Mapping Clustering Protocol running...");


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
            for (BloomFilterEncodedRecord r : c.bloomFilterEncodedRecordsSet()) {
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
