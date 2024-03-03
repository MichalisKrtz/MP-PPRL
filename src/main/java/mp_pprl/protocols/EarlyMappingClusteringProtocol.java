package mp_pprl.protocols;

import mp_pprl.domain.Record;
import mp_pprl.domain.RecordIdentifier;
import mp_pprl.encoding.CountingBloomFilter;
import mp_pprl.graph.Edge;
import mp_pprl.graph.Cluster;
import mp_pprl.graph.WeightedGraph;
import mp_pprl.optimization.HungarianAlgorithm;

import java.util.*;


public class EarlyMappingClusteringProtocol {
    private final List<Party> parties;
    private final Set<String> unionOfBlocks;
    private final double similarityThreshold;
    private final int minimumSubsetSize;
    private final int bloomFilterLength;

    public EarlyMappingClusteringProtocol(List<Party> parties, Set<String> unionOfBlocks, double similarityThreshold, int minimumSubsetSize, int bloomFilterLength) {
        this.parties = parties;
        this.unionOfBlocks = unionOfBlocks;
        this.similarityThreshold = similarityThreshold;
        this.minimumSubsetSize = minimumSubsetSize;
        this.bloomFilterLength = bloomFilterLength;
    }

    public Set<Cluster> execute(boolean enhancedPrivacy) {
        // Initialization
        WeightedGraph graph = new WeightedGraph();
        Set<Cluster> finalClusters = new HashSet<>();
        // Order parties based on database size
        orderPartiesDesc();
        // Iterate blocks
        System.out.println("Number of blocks: " + unionOfBlocks.size());
        int currentBlock = 0;
        for (String blockKey : unionOfBlocks) {
            System.out.println("Current Block: " + currentBlock);
            currentBlock++;
            WeightedGraph blockGraph = new WeightedGraph();
            for (int i = 0; i < parties.size(); i++) {
                if(enhancedPrivacy) {
                    List<Party> participantParties = getParticipantParties(i);
                    encodeBlockOfParties(participantParties, blockKey);
                }

                if (!parties.get(i).getRecordIdentifierGroups().containsKey(blockKey)) {
                    continue;
                }

                List<RecordIdentifier> block = parties.get(i).getRecordIdentifierGroups().get(blockKey);

                if (blockGraph.getClusters().isEmpty()) {
                    for (RecordIdentifier recordIdentifier : block) {
                        Cluster cluster = new Cluster(recordIdentifier);
                        blockGraph.addCluster(cluster);
                    }
                    continue;
                }

                Set<Cluster> newClusters = new HashSet<>();
                for (RecordIdentifier recordIdentifier : block) {
                    Cluster newCluster = new Cluster(recordIdentifier);
                    newClusters.add(newCluster);
                    for (Cluster cluster : blockGraph.getClusters()) {
                        List<CountingBloomFilter> cbfList = new ArrayList<>();
                        for (RecordIdentifier clusteredRecordIdentifier : cluster.recordIdentifierList()) {
                            List<RecordIdentifier> recordIdentifiersForSummation = new ArrayList<>();
                            // Add one clustered record.
                            recordIdentifiersForSummation.add(clusteredRecordIdentifier);
                            // Add the record from the new singleton cluster.
                            recordIdentifiersForSummation.add(recordIdentifier);
                            CountingBloomFilter cbf = SummationProtocol.execute(recordIdentifiersForSummation, bloomFilterLength);
                            cbfList.add(cbf);
                        }

                        double similarity = SimilarityCalculator.calculateAverageSimilarity(cbfList);
                        if (similarity >= similarityThreshold) {
                            blockGraph.addEdge(cluster, newCluster, similarity);
                        }
                    }
                }
                // Add new records to the block's graph.
                blockGraph.addClusters(newClusters);
                // Find optimal edges.
                Set<Edge> optimalEdges = HungarianAlgorithm.computeAssignments(blockGraph.getEdges());
                // Prune edges that are not optimal.
                blockGraph.getEdges().removeIf(e -> !optimalEdges.contains(e));
                // Merge clusters.
                blockGraph.mergeClusters();
            }

            graph.addClusters(blockGraph.getClusters());
        }

        for (Cluster cluster : graph.getClusters()) {
            if (cluster.recordIdentifierList().size() >= minimumSubsetSize) {
                finalClusters.add(cluster);
            }
        }

        return finalClusters;
    }

    private void encodeParties(List<Party> participantParties) {
        for (Party party : participantParties) {
            party.encodeRecords();
        }
    }

    private void encodeBlockOfParties(List<Party> participantParties, String block) {
        for (Party party : participantParties) {
            party.encodeRecordsOfBlock(block);
        }
    }

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
