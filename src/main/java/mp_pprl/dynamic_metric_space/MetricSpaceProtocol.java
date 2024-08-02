package mp_pprl.dynamic_metric_space;

import mp_pprl.core.Party;
import mp_pprl.core.domain.RecordIdentifier;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class MetricSpaceProtocol {
    private final List<Party> parties;
    private static final double MAXIMAL_INTERSECTION = 0.12;
    private static final double SIMILARITY_THRESHOLD = 0.75;
    public MetricSpaceProtocol(List<Party> parties) {
        this.parties = parties;
    }

    public void run() {
        Set<Pivot> pivots = new HashSet<>();

        MetricSpace metricSpace = new MetricSpace();
        Indexer indexer = new Indexer(metricSpace);
        List<RecordIdentifier> recordIdentifiers1 = parties.getFirst().getRecordIdentifiers();
        indexer.setInitialPivots(recordIdentifiers1);
        indexer.assignElementsToPivots(recordIdentifiers1, MAXIMAL_INTERSECTION);
        for (int i = 1; i < parties.size(); i++) {
            indexer.assignElementsToPivots(parties.get(i).getRecordIdentifiers(), MAXIMAL_INTERSECTION);
        }
        metricSpace.printMetricSpace();
    }

}
