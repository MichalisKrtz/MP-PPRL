package mp_pprl.incremental_clustering;

import mp_pprl.PPRLProtocol;
import mp_pprl.RecordIdentifier;
import mp_pprl.RecordIdentifierCluster;
import mp_pprl.core.BloomFilterEncodedRecord;
import mp_pprl.core.graph.Edge;
import mp_pprl.core.graph.Cluster;
import mp_pprl.core.graph.WeightedGraph;
import mp_pprl.incremental_clustering.optimization.HungarianAlgorithm;
import mp_pprl.core.Party;

import java.util.*;
import java.util.stream.Collectors;


public class EarlyMappingClusteringProtocol implements PPRLProtocol {
    private final double similarityThreshold;
    private final List<Party> parties;
    private final Set<String> unionOfBlocks;
    private final int minimumSubsetSize;
    private final int bloomFilterLength;
    private final boolean enhancedPrivacy;
    private final Set<Cluster> finalClusters;

    public EarlyMappingClusteringProtocol(List<Party> parties, Set<String> unionOfBlocks, double similarityThreshold, int minimumSubsetSize, int bloomFilterLength, boolean enhancedPrivacy) {
        this.parties = parties;
        this.unionOfBlocks = unionOfBlocks;
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
        System.out.println("Number of blocks: " + unionOfBlocks.size());
        for (String blockKey : unionOfBlocks) {
            WeightedGraph blockGraph = new WeightedGraph();
            for (int i = 0; i < parties.size(); i++) {
                if(enhancedPrivacy) {
                    List<Party> participantParties = getParticipantParties(i);
//                    encodeBlockOfParties(participantParties, blockKey);
                }

                if (!parties.get(i).getBloomFilterEncodedRecordGroups().containsKey(blockKey)) {
                    continue;
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
                        double similarity;
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
                Set<Edge> optimalEdges = HungarianAlgorithm.computeAssignments(blockGraph.getEdges(), true);
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
                        .map(encodedRecord -> new RecordIdentifier(encodedRecord.party(), encodedRecord.id()))
                        .collect(Collectors.toSet()))
                .map(RecordIdentifierCluster::new)
                .collect(Collectors.toSet());
    }

//    private void encodeBlockOfParties(List<Party> participantParties, String block) {
//        EncodingHandler encodingHandler = new EncodingHandler();
//        for (Party party : participantParties) {
//            party.encodeRecordsOfBlock(encodingHandler, block);
//        }
//    }

    private List<Party> getParticipantParties(int indexOfCurrentParty) {
        ArrayList<Party> participantParties = new ArrayList<>();
        for (int i = 0; i <= indexOfCurrentParty; i++) {
            participantParties.add(parties.get(i));
        }

        return participantParties;
    }

    private void orderPartiesDesc() {
        Comparator<Party> comp = Comparator.comparingInt(Party::getRecordsSize);
        parties.sort(comp.reversed());
    }

}
