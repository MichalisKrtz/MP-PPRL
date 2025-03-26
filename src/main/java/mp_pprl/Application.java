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
    private static final int minimumSubsetSize = 3;
    private static final String[] blockingKeyValues = {"first_name", "last_name"};
    private static final String[] quasiIdentifiers = {"id", "first_name", "last_name",};
    //    private static final String[] quasiIdentifiers = {"id", "first_name", "last_name", "middle_name", "address", "city"};
    // Databases
    private final static List<List<String>> dbPathGroups = new ArrayList<>();
    private static List<String> dbPaths = new ArrayList<>();


    public Application() {
        dbPathGroups.add(Arrays.asList(
                "/home/michalis/Development/Thesis/Dev/MP-PPRL Databases/authors_2/A_200000.db",
                "/home/michalis/Development/Thesis/Dev/MP-PPRL Databases/authors_2/B_1_200000.db",
                "/home/michalis/Development/Thesis/Dev/MP-PPRL Databases/authors_2/C_1_200000.db")
        );

        dbPathGroups.add(Arrays.asList(
                "/home/michalis/Development/Thesis/Dev/MP-PPRL Databases/authors_2/A_200000.db",
                "/home/michalis/Development/Thesis/Dev/MP-PPRL Databases/authors_2/B_1_200000.db",
                "/home/michalis/Development/Thesis/Dev/MP-PPRL Databases/authors_2/C_1_200000.db",
                "/home/michalis/Development/Thesis/Dev/MP-PPRL Databases/authors_2/D_1_200000.db")
        );
    }

    public void run() {
        for (List<String> dbGroup : dbPathGroups) {
            dbPaths = dbGroup;
            long startTime = System.currentTimeMillis();
            // SB
            runSoundexBasedProtocol(0, 0, false);
            // S-SB
            runSoundexBasedProtocol(0, 2, true);
            // EMIC
            runEarlyMappingClusteringProtocol(0, false);
            // T-EMIC
            runEarlyMappingClusteringProtocol(3, false);
            // DMS
            runMetricSpaceProtocol(false, 0);
            // B-DMS
            runMetricSpaceProtocol(true, 3);
            long endTime = System.currentTimeMillis();
            System.out.print("Time taken: " + (endTime - startTime) + "ms\n\n");
        }

    }

    public static void runSoundexBasedProtocol(double noisePercentage, int charsToTruncate, boolean splitFields) {
        System.out.println("Soundex Based Protocol running...");

        List<Party> parties = loadRecordsToParties();
        int numberOfRecords = parties.getFirst().getRecordsSize();
        encodePartyRecordsToSoundex(parties, splitFields);
        generateNoiseData(parties, noisePercentage);
        truncateData(parties, charsToTruncate);
        generateHashes(parties);

        System.out.println("Soundex based protocol...");
        SoundexBasedProtocol soundexBasedProtocol = new SoundexBasedProtocol(parties);
        PerformanceMetrics metrics = new PerformanceMetrics(soundexBasedProtocol, parties.size(), numberOfRecords, 0.25);
        printProtocolResults(metrics);
    }

    public static void runEarlyMappingClusteringProtocol(int charsToTruncateFromSoundex, boolean enhancedPrivacy) {
        double similarityThreshold = 0.8;

        System.out.println("Early Mapping Clustering Protocol running...");

        List<Party> parties = loadRecordsToParties();
        encodePartyRecordsToBloomFilters(parties);
        groupPartyRecords(parties, charsToTruncateFromSoundex);

        System.out.println("Early mapping clustering protocol...");
        Set<String> unionOfBKVs = getUnionOfBKVs(parties);
        EarlyMappingClusteringProtocol clusteringProtocol = new EarlyMappingClusteringProtocol(parties, unionOfBKVs, similarityThreshold, minimumSubsetSize, bloomFilterLength, enhancedPrivacy);
        System.out.println("Number of blocks: " + unionOfBKVs.size());
        PerformanceMetrics metrics = new PerformanceMetrics(clusteringProtocol, parties.size(),parties.getFirst().getRecordsSize(), 0.25);
        printProtocolResults(metrics);
    }

    public static void runMetricSpaceProtocol(boolean blocking, int charsToTruncateFromSoundex) {
        double maximalIntersection = 0.003;
        double similarityThreshold = 0.8;
        if (!blocking) {
            similarityThreshold = 0.85;
        }
        Set<String> unionOfBKVs = null;

        System.out.println("Metric Space Protocol running...");
        List<Party> parties = loadRecordsToParties();
        encodePartyRecordsToBloomFilters(parties);
        if (blocking) {
            groupPartyRecords(parties, charsToTruncateFromSoundex);
            unionOfBKVs = getUnionOfBKVs(parties);
        }

        System.out.println("Metric space protocol...");
        MetricSpaceProtocol metricSpaceProtocol = new MetricSpaceProtocol(parties, maximalIntersection, similarityThreshold, blocking, unionOfBKVs);
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

    private static void encodePartyRecordsToSoundex(List<Party> parties, boolean splitFields) {
        System.out.println("Encoding party records with soundex...");
        for (Party party : parties) {
            party.encodeRecordsWithSoundex(splitFields);
        }
    }

    private static List<Party> loadRecordsToParties() {
        System.out.println("Retrieving records from the databases...");
        int numberOfParties = dbPaths.size();
        List<RecordRepository> recordRepositories = new ArrayList<>();
        List<List<Record>> listsOfPartyRecords = new ArrayList<>();
        for (int i = 0; i < numberOfParties; i++) {
            recordRepositories.add(new SQLiteRecordRepository(dbPaths.get(i)));
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

    private static void groupPartyRecords(List<Party> parties, int charsToTruncateFromSoundex) {
        System.out.println("Grouping party records...");
        for (Party party : parties) {
            party.groupBloomFilterEncodedRecordsByBlockingKeyValue(charsToTruncateFromSoundex);
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
        for (int i = 0; i < 50; i++) {
            System.out.print("=");
        }
        System.out.println();
        System.out.println("Runtime: " + metrics.getRunTime());
        System.out.println("Precision: " + metrics.calculatePrecision());
        System.out.println("Recall: " + metrics.calculateRecall());
        System.out.println("F1: " + metrics.calculateF1());
        System.out.println("Protocol finished successfully");
        for (int i = 0; i < 50; i++) {
            System.out.print("=");
        }
        System.out.println();
    }
}
