package mp_pprl.secondary_encoding;

import mp_pprl.PPRLProtocol;
import mp_pprl.RecordIdentifier;
import mp_pprl.RecordIdentifierCluster;
import mp_pprl.core.BloomFilterEncodedRecord;
import mp_pprl.core.Party;
import mp_pprl.core.graph.Cluster;

import java.util.*;
import java.util.stream.Collectors;

/* -----------------------
 * Linker: high-level linkage using encodings + Dice backtracking over inconsistent splits
 * ----------------------- */
public class ISEProtocol implements PPRLProtocol {
    final int m;
    final int a;
    final int k;
    final double alpha;
    private final Set<Cluster> finalClusters;
    private final List<Party> parties;

    public ISEProtocol(List<Party> parties, int m, int a, int k, double alpha) {
        this.parties = parties;
        this.m = m; this.a = a; this.k = k; this.alpha = alpha;
        this.finalClusters = new HashSet<>();

    }

    public int errorLimit() {
        return Math.max(0, m - a * k);
    }

    /**
     * Execute the ISE/PPRL protocol across all parties
     * Returns clusters of matched records
     */
    public void execute() {
        // Get all bloom filter encoded records from all parties
        System.out.println("Starting ISE protocol...");
        List<List<BloomFilterEncodedRecord>> partyRecords = new ArrayList<>();
        for (Party party : parties) {
            partyRecords.add(party.getBloomFilterEncodedRecords());
        }

        // Find the maximum number of records across all parties
        int maxRecords = partyRecords.stream()
                .mapToInt(List::size)
                .max()
                .orElse(0);

        // Compare records at each index across all parties
        for (int recordIdx = 0; recordIdx < maxRecords; recordIdx++) {
            List<BloomFilterEncodedRecord> recordsToCompare = new ArrayList<>();

            // Collect the record at this index from each party (if it exists)
            for (List<BloomFilterEncodedRecord> partyRecordList : partyRecords) {
                if (recordIdx < partyRecordList.size()) {
                    recordsToCompare.add(partyRecordList.get(recordIdx));
                }
            }

            // Only compare if we have records from multiple parties
            if (recordsToCompare.size() >= 2) {
                if (linkRecords(recordsToCompare)) {
                    // Create cluster for matched records
                    Cluster cluster = new Cluster(recordsToCompare);
                    finalClusters.add(cluster);
                }
            }
        }
    }

    public Set<RecordIdentifierCluster> getResults() {
        return finalClusters.stream()
                .map(cluster -> cluster.bloomFilterEncodedRecordsSet().stream()
                        .map(encodedRecord -> new RecordIdentifier(encodedRecord.getParty(), encodedRecord.getId()))
                        .collect(Collectors.toSet()))
                .map(RecordIdentifierCluster::new)
                .collect(Collectors.toSet());
    }

    /**
     * Link a set of records using ISE/PPRL algorithm
     *
     * High-level linking:
     *  - If all m splits identical -> match
     *  - If identicalCount < e -> no match
     *  - Else: compute Dice over inconsistent splits only (multi-party formula)
     */
    private boolean linkRecords(List<BloomFilterEncodedRecord> records) {
        int s = records.size();

        // Extract secondary encodings and splits for all records
        List<List<SecondaryEncoding>> encodingsList = new ArrayList<>();
        List<List<byte[]>> splitsList = new ArrayList<>();

        for (BloomFilterEncodedRecord record : records) {
            Party party = record.getParty();
            int recordIndex = party.getBloomFilterEncodedRecords().indexOf(record);

            encodingsList.add(party.getEncPerRecord().get(recordIndex));
            splitsList.add(party.getSplitsPerRecord().get(recordIndex));
        }

        // Count identical splits
        int identicalCount = 0;
        List<Integer> inconsistent = new ArrayList<>();

        for (int j = 0; j < m; j++) {
            boolean allSame = true;
            SecondaryEncoding base = encodingsList.get(0).get(j);

            for (int i = 1; i < s; i++) {
                if (!base.equals(encodingsList.get(i).get(j))) {
                    allSame = false;
                    break;
                }
            }

            if (allSame) {
                identicalCount++;
            } else {
                inconsistent.add(j);
            }
        }

        // Rule 1: If all m splits identical -> match
        if (identicalCount == m) {
            return true;
        }

        // Rule 3: If identicalCount < e -> no match
        int e = errorLimit();
        if (identicalCount < e) {
            return false;
        }

        // Rule 2: Compute Dice over inconsistent splits only
        long numeratorSum = 0;   // sum of intersections across inconsistent splits
        long denominatorSum = 0; // sum of per-party ones across inconsistent splits

        for (int splitIdx : inconsistent) {
            // Compute intersection count for this split
            byte[] intersection = splitsList.get(0).get(splitIdx).clone();

            for (int p = 1; p < s; p++) {
                byte[] currentSplit = splitsList.get(p).get(splitIdx);
                for (int b = 0; b < intersection.length; b++) {
                    intersection[b] = (byte) (intersection[b] & currentSplit[b]);
                }
            }

            int interCount = countOnes(intersection);
            numeratorSum += interCount;

            // Add per-party ones for this split
            for (int p = 0; p < s; p++) {
                denominatorSum += countOnes(splitsList.get(p).get(splitIdx));
            }
        }

        if (denominatorSum == 0) {
            return false;
        }

        double dice = (s * (double) numeratorSum) / (double) denominatorSum;
        return dice >= alpha;
    }

    /**
     * Count number of 1s in a byte array
     */
    private int countOnes(byte[] bits) {
        int count = 0;
        for (byte b : bits) {
            if (b == 1) count++;
        }
        return count;
    }
}
