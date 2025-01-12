package mp_pprl;

import mp_pprl.core.data.SQLiteRecordRepository;
import mp_pprl.core.domain.Record;
import mp_pprl.core.encoding.EncodingHandler;
import mp_pprl.incremental_clustering.EarlyMappingClusteringProtocol;
import mp_pprl.dynamic_metric_space.MetricSpaceProtocol;
import mp_pprl.core.Party;
import mp_pprl.core.domain.RecordRepository;
import mp_pprl.soundex_based.SoundexBasedProtocol;

import java.util.*;

public class Application {
    // Parameters
    private static final int bloomFilterLength = 1000;
    private static final int numberOfHashFunctions = 30;
//    private static final String[] quasiIdentifiers = {"id", "first_name", "last_name"};
    private static final String[] quasiIdentifiers = {"id", "first_name", "last_name", "middle_name", "address", "age"};
    private static final String[] blockingKeyValues = {"first_name", "last_name"};
    private static final int minimumSubsetSize = 3;
    // Databases
    private final static String[] dbPaths = {
            "/home/michalis/Development/Thesis/Dev/MP-PPRL Databases/MP_10000/MP_A_10000.db",
            "/home/michalis/Development/Thesis/Dev/MP-PPRL Databases/MP_10000/MP_B_1_10000.db",
            "/home/michalis/Development/Thesis/Dev/MP-PPRL Databases/MP_10000/MP_C_1_10000.db",
    };

    public static void run() {
        long startTime = System.currentTimeMillis();
//        runSoundexBasedProtocol();
        runEarlyMappingClusteringProtocol(true);
//        runMetricSpaceProtocol();
        long endTime = System.currentTimeMillis();
        System.out.println("Time taken: " + (endTime - startTime) + "ms");
    }

    public static void runSoundexBasedProtocol() {
        System.out.println("Soundex Based Protocol running...");
        double noisePercentage = 0;
        int charsToTruncate = 0;

        List<Party> parties = loadRecordsToParties();
        int numberOfRecords = parties.getFirst().getRecordsSize();
        encodePartyRecordsToSoundex(parties);
        generateNoiseData(parties, noisePercentage);
        truncateData(parties, charsToTruncate);
        generateHashes(parties);

        System.out.println("Soundex based protocol...");
        SoundexBasedProtocol soundexBasedProtocol = new SoundexBasedProtocol(parties);
        PerformanceMetrics metrics = new PerformanceMetrics(soundexBasedProtocol, parties.size(), numberOfRecords, 0.25);
        printProtocolResults(metrics);
    }

    public static void runEarlyMappingClusteringProtocol(boolean enhancedPrivacy) {
        System.out.println("Early Mapping Clustering Protocol running...");
        double similarityThreshold = 0.8;

        List<Party> parties = loadRecordsToParties();
        encodePartyRecordsToBloomFilters(parties);
        groupPartyRecords(parties);

        System.out.println("Early mapping clustering protocol...");
        Set<String> unionOfBKVs = getUnionOfBKVs(parties);
        EarlyMappingClusteringProtocol clusteringProtocol = new EarlyMappingClusteringProtocol(parties, unionOfBKVs, similarityThreshold, minimumSubsetSize, bloomFilterLength, enhancedPrivacy);
        PerformanceMetrics metrics = new PerformanceMetrics(clusteringProtocol, parties.size(),parties.getFirst().getRecordsSize(), 0.25);
        printProtocolResults(metrics);
    }

    public static void runMetricSpaceProtocol() {
        System.out.println("Metric Space Protocol running...");
        double similarityThreshold = 0.85;
        double maximalIntersection = 0.003;

        List<Party> parties = loadRecordsToParties();
        encodePartyRecordsToBloomFilters(parties);

        System.out.println("Metric space protocol...");
        MetricSpaceProtocol metricSpaceProtocol = new MetricSpaceProtocol(parties, maximalIntersection, similarityThreshold);
        PerformanceMetrics metrics = new PerformanceMetrics(metricSpaceProtocol, parties.size(),parties.getFirst().getRecordsSize(), 0.25);
        printProtocolResults(metrics);
    }

    private static void generateHashes(List<Party> parties) {
        System.out.println("Generating hashes...");
        EncodingHandler encodingHandler = new EncodingHandler();
        for (Party party : parties) {
            party.generateHashesForSoundexEncodedRecords(encodingHandler);
        }
    }

    private static void truncateData(List<Party> parties, int charsToTruncate) {
        System.out.println("Truncate data...");
        for (Party party : parties) {
            party.truncateSoundexEncodedRecords(charsToTruncate);
        }
    }

    private static void generateNoiseData(List<Party> parties, double noisePercentage) {
        System.out.println("Generating noise data...");
        for (Party party : parties) {
            party.generateNoise(noisePercentage);
        }
    }

    private static void encodePartyRecordsToSoundex(List<Party> parties) {
        System.out.println("Encoding party records with soundex...");
        for (Party party : parties) {
            party.encodeRecordsWithSoundex();
        }
    }

    private static List<Party> loadRecordsToParties() {
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
            parties.add(new Party(i, quasiIdentifiers, blockingKeyValues, bloomFilterLength, numberOfHashFunctions));
            parties.get(i).addRecords(listsOfPartyRecords.get(i));
        }
        return parties;
    }

    private static void encodePartyRecordsToBloomFilters(List<Party> parties) {
        System.out.println("Encoding party records...");
        EncodingHandler encodingHandler = new EncodingHandler();
        for (Party party : parties) {
            party.encodeRecords(encodingHandler);
        }
    }

    private static void groupPartyRecords(List<Party> parties) {
        System.out.println("Grouping party records...");
        for (Party party : parties) {
            party.groupBloomFilterEncodedRecordsByBlockingKeyValue();
        }
    }

    private static Set<String> getUnionOfBKVs(List<Party> parties) {
        Set<String> blocks = new HashSet<>();
        for (Party party : parties) {
            blocks.addAll(party.getBloomFilterEncodedRecordGroups().keySet());
        }

        return blocks;
    }

    private static void printProtocolResults(PerformanceMetrics metrics) {
        metrics.run();
//        metrics.printClusters();
        System.out.println("Runtime: " + metrics.getRunTime());
        System.out.println("Precision: " + metrics.calculatePrecision());
        System.out.println("Recall: " + metrics.calculateRecall());
        System.out.println("F1: " + metrics.calculateF1());
        System.out.println("Protocol finished successfully");
    }
}
