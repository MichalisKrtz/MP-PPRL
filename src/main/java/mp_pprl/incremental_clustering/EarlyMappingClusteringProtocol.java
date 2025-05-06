package mp_pprl.incremental_clustering;

import mp_pprl.PPRLProtocol;
import mp_pprl.RecordIdentifier;
import mp_pprl.RecordIdentifierCluster;
import mp_pprl.core.BloomFilterEncodedRecord;
import mp_pprl.core.encoding.EncodingHandler;
import mp_pprl.core.graph.Edge;
import mp_pprl.core.graph.Cluster;
import mp_pprl.core.graph.WeightedGraph;
import mp_pprl.core.Party;
import mp_pprl.core.optimization.HungarianAlgorithmMem;

import java.util.*;
import java.util.stream.Collectors;


public class EarlyMappingClusteringProtocol implements PPRLProtocol {
    private final float similarityThreshold;
    private final List<Party> parties;
    private final Set<String> unionOfBKVs;
    private final int minimumSubsetSize;
    private final int bloomFilterLength;
    private final boolean enhancedPrivacy;
    private final Set<Cluster> finalClusters;

    public EarlyMappingClusteringProtocol(List<Party> parties, Set<String> unionOfBKVS, float similarityThreshold, int minimumSubsetSize, int bloomFilterLength, boolean enhancedPrivacy) {
        this.parties = parties;
        this.unionOfBKVs = unionOfBKVS;
        this.similarityThreshold = similarityThreshold;
        this.minimumSubsetSize = minimumSubsetSize;
        this.bloomFilterLength = bloomFilterLength;
        this.enhancedPrivacy = enhancedPrivacy;
        this.finalClusters = new HashSet<>();
    }

    public void execute() {
        // Initialization
        WeightedGraph graph = new WeightedGraph();
        // Order parties based on database size
        orderPartiesDesc();
        // Iterate blocks
        System.out.println("Number of blocks: " + unionOfBKVs.size());
        for (String blockKey : unionOfBKVs) {
            WeightedGraph blockGraph = new WeightedGraph();
            for (int i = 0; i < parties.size(); i++) {
                if (!parties.get(i).getBloomFilterEncodedRecordGroups().containsKey(blockKey)) {
                    continue;
                }

                if(enhancedPrivacy) {
                    List<Party> participantParties = getParticipantParties(i, blockKey);
                    encodeBlockOfParties(participantParties, blockKey);
                }

                List<BloomFilterEncodedRecord> block = parties.get(i).getBloomFilterEncodedRecordGroups().get(blockKey);

                if (blockGraph.getClusters().isEmpty()) {
                    for (BloomFilterEncodedRecord bloomFilterEncodedRecord : block) {
                        Cluster cluster = new Cluster(bloomFilterEncodedRecord);
                        blockGraph.addCluster(cluster);
                    }
                    continue;
                }

                Set<Cluster> newClusterSet = new HashSet<>();
                for (BloomFilterEncodedRecord bloomFilterEncodedRecord : block) {
                    Cluster newCluster = new Cluster(bloomFilterEncodedRecord);
                    newClusterSet.add(newCluster);
                    for (Cluster cluster : blockGraph.getClusters()) {
                        float similarity;
                        if (enhancedPrivacy) {
                            similarity = SimilarityCalculator.averageSimilaritySecure(cluster, bloomFilterEncodedRecord, bloomFilterLength);
                        } else {
                            similarity = SimilarityCalculator.averageSimilarity(cluster, bloomFilterEncodedRecord);
                        }

                        if (similarity >= similarityThreshold) {
                            blockGraph.addEdge(cluster, newCluster, similarity);
                        }
                    }
                }
                // Add new records to the block's graph.
                blockGraph.addClusters(newClusterSet);
                // Find optimal edges.
                Set<Edge> optimalEdges = HungarianAlgorithmMem.computeAssignments(blockGraph.getEdges(), true);
                // Prune edges that are not optimal.
                blockGraph.getEdges().removeIf(e -> !optimalEdges.contains(e));
                // Merge clusters.
                blockGraph.mergeClusters();
            }

            graph.addClusters(blockGraph.getClusters());
        }

        for (Cluster cluster : graph.getClusters()) {
            if (cluster.bloomFilterEncodedRecordsSet().size() >= minimumSubsetSize) {
                finalClusters.add(cluster);
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

    private void encodeBlockOfParties(List<Party> participantParties, String block) {
        EncodingHandler encodingHandler = new EncodingHandler();
        for (Party party : participantParties) {
            party.encodeRecordsOfBlock(encodingHandler, block);
        }
    }

    private List<Party> getParticipantParties(int indexOfCurrentParty, String blockKey) {
        ArrayList<Party> participantParties = new ArrayList<>();
        for (int i = 0; i <= indexOfCurrentParty; i++) {
            if (!parties.get(i).getBloomFilterEncodedRecordGroups().containsKey(blockKey)) {
                continue;
            }
            participantParties.add(parties.get(i));
        }

        return participantParties;
    }

    private void orderPartiesDesc() {
        Comparator<Party> comp = Comparator.comparingInt(Party::getRecordsSize);
        parties.sort(comp.reversed());
    }

}
