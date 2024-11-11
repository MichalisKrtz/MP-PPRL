package mp_pprl.soundex_based;

import mp_pprl.PPRLProtocol;
import mp_pprl.RecordIdentifier;
import mp_pprl.RecordIdentifierCluster;
import mp_pprl.core.Party;

import java.util.*;
import java.util.stream.Collectors;

public class SoundexBasedProtocol implements PPRLProtocol {
    private final List<Party> parties;
    private final Map<String, Set<HashedSoundexEncodedRecord>> recordsMap;

    public SoundexBasedProtocol(List<Party> parties) {
        this.parties = parties;
        recordsMap = new HashMap<>();
    }

    public void execute() {
        for (Party party : parties) {
            for (HashedSoundexEncodedRecord encodedRecord : party.getHashedSoundexEncodedRecords()) {
                if (!recordsMap.containsKey(encodedRecord.hash())) {
                    recordsMap.put(encodedRecord.hash(), new HashSet<>());
                    recordsMap.get(encodedRecord.hash()).add(encodedRecord);
                    continue;
                }
                recordsMap.get(encodedRecord.hash()).add(encodedRecord);
            }
        }
    }

    public Set<RecordIdentifierCluster> getResults() {
        return recordsMap.values().stream()
                .map(records -> records.stream()
                        .map(encodedRecord -> new RecordIdentifier(encodedRecord.party(), encodedRecord.id()))
                        .collect(Collectors.toSet()))
                .map(RecordIdentifierCluster::new)
                .collect(Collectors.toSet());
    }


    private static void printRecordsMap(Map<String, List<HashedSoundexEncodedRecord>> recordsMap) {
        for (String hash : recordsMap.keySet()) {
            System.out.print("hash" + ": ");
            for (HashedSoundexEncodedRecord hashedSoundexEncodedRecord : recordsMap.get(hash)) {
                System.out.print(hashedSoundexEncodedRecord.party() + "." + hashedSoundexEncodedRecord.id() + ", ");
            }
            System.out.println();
        }
    }
}
