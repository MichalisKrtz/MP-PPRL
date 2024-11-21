package mp_pprl;

import java.util.Set;

public interface PPRLProtocol {
    void execute();
    Set<RecordIdentifierCluster> getResults();
}
