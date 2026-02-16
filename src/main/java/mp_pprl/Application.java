package mp_pprl;

import mp_pprl.core.data.SQLiteRecordRepository;
import mp_pprl.core.domain.Record;
import mp_pprl.core.encoding.EncodingHandler;
import mp_pprl.incremental_clustering.EarlyMappingClusteringProtocol;
import mp_pprl.dynamic_metric_space.MetricSpaceProtocol;
import mp_pprl.core.Party;
import mp_pprl.core.domain.RecordRepository;
import mp_pprl.soundex_based.SoundexBasedProtocol;
import mp_pprl.secondary_encoding.ISEProtocol;

import java.util.*;

public class Application {
    // Parameters
    private static final int bloomFilterLength = 1000;
    private static final int numberOfHashFunctions = 30;
    private static final int minimumSubsetSize = 3;
    private static final String[] blockingKeyValues = {"first_name", "last_name"};
    private static String[] quasiIdentifiers;
    //private static final String[] quasiIdentifiers = {"id", "first_name", "last_name"};
    //private static final String[] quasiIdentifiers = {"id", "first_name", "last_name", "middle_name", "address", "city"};
    // Databases
    private final static List<List<String>> dbPathGroups = new ArrayList<>();
    private static List<String> dbPaths = new ArrayList<>();
    private String method;

    public static <T> List<T> pickRandom(T[] array, int n, long seed) {
        List<T> list = new ArrayList<>(Arrays.asList(array));
        Collections.shuffle(list, new Random(seed));
        return list.subList(0, n);
    }

    public Application(String dataset, int datasetSize, int errors, int numParties, String method) {
        String localPath = "";

        String[] maxParties = {"B", "C", "D", "E"};

        this.method = method;

        if ("authors".equals(dataset)) {
            quasiIdentifiers = new String[] {
                    "id", "first_name", "last_name"
            };
            localPath += "/authors_2/";
        } else if ("voters".equals(dataset)) {
            quasiIdentifiers = new String[] {
                    "id", "first_name", "last_name",
                    "middle_name", "address", "city"
            };
            localPath += "/MP/MP_";
        }

        // Shuffle all parties except A
        List<String> parties =
                pickRandom(maxParties, numParties-1, System.currentTimeMillis());
        parties.add(0, "A");
        System.out.println(Arrays.toString(parties.toArray()));

        for (int i = 0; i < parties.size(); i++) {
            String party = parties.get(i);
            String db_filename = null;
            if (party=="A")
                db_filename = localPath + parties.get(i) + "_" + datasetSize + ".db";
            else
                db_filename = localPath + parties.get(i) + "_"+ errors + "_" + datasetSize + ".db";
            parties.set(i, db_filename);
            System.out.println(db_filename);
        }

        dbPathGroups.add(new ArrayList<>(parties));
    }

    public Application() {
        String localPath = "/home/agroml1/MP-PPRL-dbs";

/*
        dbPathGroups.add(Arrays.asList(
                localPath+"/authors_2/A_50000.db",
                localPath+"/authors_2/B_1_50000.db",
                localPath+"/authors_2/C_1_50000.db"//,
                //localPath+"/authors_2/D_1_50000.db",
                //localPath+"/authors_2/E_1_50000.db"
                )
        );
*/

        dbPathGroups.add(Arrays.asList(
                        localPath+"/MP/MP_A_500000.db",
                        localPath+"/MP/MP_B_5_500000.db",
                        localPath+"/MP/MP_C_5_500000.db",
                        localPath+"/MP/MP_D_5_500000.db",
                        localPath+"/MP/MP_E_5_500000.db"
                )
        );

    }

    public void run(String selectedMethod) {
        for (List<String> dbGroup : dbPathGroups) {
            dbPaths = dbGroup;
            long startTime = System.currentTimeMillis();

            switch(selectedMethod) {
                case "SB":
                    runSoundexBasedProtocol(0, 0, false);
                    break;
                case "S-SB":
                    runSoundexBasedProtocol(0, 2, true);
                    break;
                case "EMIC":
                    runEarlyMappingClusteringProtocol(0, false);
                    break;
                case "T-EMIC":
                    runEarlyMappingClusteringProtocol(3, false);
                    break;
                case "DMS":
                    runMetricSpaceProtocol(false, 0);
                    break;
                case "B-DMS":
                    runMetricSpaceProtocol(true, 3);
                    break;
                case "ISE":
                    runImprovedSecondaryEncodingProtocol();
                    break;
                default:
                    System.out.println("Unsupported Method");
                    System.exit(-1);
            }
            long endTime = System.currentTimeMillis();
            System.out.print("Time taken: " + (endTime - startTime) + "ms\n\n");
        }
    }
    public void run() {
        for (List<String> dbGroup : dbPathGroups) {
            dbPaths = dbGroup;
            long startTime = System.currentTimeMillis();
            // SB
            // runSoundexBasedProtocol(0, 0, false); // has run
            // S-SB
            // runSoundexBasedProtocol(0, 2, true); // has run
            // EMIC
            //runEarlyMappingClusteringProtocol(0, false); //
            //T-EMIC
            // runEarlyMappingClusteringProtocol(3, false); //
            // DMS
            runMetricSpaceProtocol(false, 0); //
            //B-DMS
            //runMetricSpaceProtocol(true, 3); //
            long endTime = System.currentTimeMillis();
            System.out.print("Time taken: " + (endTime - startTime) + "ms\n\n");
        }

    }

    public static void runImprovedSecondaryEncodingProtocol() {
        System.out.println("Improved Secondary Encoding Protocol running...");
        int M = 8;         // number of splits
        int A = 4;         // tolerance parameter (paper uses 'a')
        double ALPHA = 0.6; // Dice threshold
        int k = numberOfHashFunctions;
        List<Party> parties = loadRecordsToParties();
        encodePartyRecordsToBloomFilters(parties);
        splitEncodedPartyRecords(parties, M);
        computeSecondaryEncodingsOfSplitRecords(parties, M);

        System.out.println("Improved Secondary Encoding protocol...");
        ISEProtocol iseProtocol = new ISEProtocol(parties, M, A, k, ALPHA);
        int numberOfRecords = parties.getFirst().getRecordsSize();
        PerformanceMetrics metrics = new PerformanceMetrics(iseProtocol, parties.size(), numberOfRecords, 0.25f);
        printProtocolResults(metrics);
    }

    private static void computeSecondaryEncodingsOfSplitRecords(List<Party> parties, int M) {
        for (Party party : parties) {
            party.computeSecondaryEncodings(M);
        }
    }

    private static void splitEncodedPartyRecords(List<Party> parties, int M) {
        for (Party party : parties) {
            party.splitEncodedRecords(M);
        }
    }

    public static void runSoundexBasedProtocol(float noisePercentage, int charsToTruncate, boolean splitFields) {
        System.out.println("Soundex Based Protocol running...");

        List<Party> parties = loadRecordsToParties();
        int numberOfRecords = parties.getFirst().getRecordsSize();
        encodePartyRecordsToSoundex(parties, splitFields);
        generateNoiseData(parties, noisePercentage);
        truncateData(parties, charsToTruncate);
        generateHashes(parties);

        System.out.println("Soundex based protocol...");
        SoundexBasedProtocol soundexBasedProtocol = new SoundexBasedProtocol(parties);
        PerformanceMetrics metrics = new PerformanceMetrics(soundexBasedProtocol, parties.size(), numberOfRecords, 0.25f);
        printProtocolResults(metrics);
    }

    public static void runEarlyMappingClusteringProtocol(int charsToTruncateFromSoundex, boolean enhancedPrivacy) {
        float similarityThreshold = 0.8f;

        System.out.println("Early Mapping Clustering Protocol running...");

        List<Party> parties = loadRecordsToParties();
        encodePartyRecordsToBloomFilters(parties);
        groupPartyRecords(parties, charsToTruncateFromSoundex);

        System.out.println("Early mapping clustering protocol...");
        Set<String> unionOfBKVs = getUnionOfBKVs(parties);
        EarlyMappingClusteringProtocol clusteringProtocol = new EarlyMappingClusteringProtocol(parties, unionOfBKVs, similarityThreshold, minimumSubsetSize, bloomFilterLength, enhancedPrivacy);
        System.out.println("Number of blocks: " + unionOfBKVs.size());
        PerformanceMetrics metrics = new PerformanceMetrics(clusteringProtocol, parties.size(),parties.getFirst().getRecordsSize(), 0.25f);
        printProtocolResults(metrics);
    }

    public static void runMetricSpaceProtocol(boolean blocking, int charsToTruncateFromSoundex) {
        float maximalIntersection = 0.003f;
        float similarityThreshold = 0.8f;
        if (!blocking) {
            similarityThreshold = 0.85f;
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
        PerformanceMetrics metrics = new PerformanceMetrics(metricSpaceProtocol, parties.size(),parties.getFirst().getRecordsSize(), 0.25f);
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

    private static void generateNoiseData(List<Party> parties, float noisePercentage) {
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
