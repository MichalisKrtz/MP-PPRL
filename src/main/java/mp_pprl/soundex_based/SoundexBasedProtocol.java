package mp_pprl.soundex_based;

import mp_pprl.core.HashedSoundexEncodedRecord;
import mp_pprl.core.Party;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SoundexBasedProtocol {
    private final List<Party> parties;

    public SoundexBasedProtocol(List<Party> parties) {
        this.parties = parties;
    }

    public void run() {
        Map<String, List<RecordIdentifier>> recordsMap = new HashMap<>();

        for (Party party : parties) {
            for (HashedSoundexEncodedRecord encodedRecord : party.getHashedSoundexEncodedRecords()) {
                if (!recordsMap.containsKey(encodedRecord.getHash())) {
                    recordsMap.put(encodedRecord.getHash(), new ArrayList<>());
                    recordsMap.get(encodedRecord.getHash()).add(new RecordIdentifier(encodedRecord.getParty(), encodedRecord.getId()));
                    continue;
                }
                recordsMap.get(encodedRecord.getHash()).add(new RecordIdentifier(encodedRecord.getParty(), encodedRecord.getId()));
            }
        }

//        printRecordsMap(recordsMap);

        System.out.println("Number of hashes: " + recordsMap.size());
    }

    private static void printRecordsMap(Map<String, List<RecordIdentifier>> recordsMap) {
        for (String hash : recordsMap.keySet()) {
            System.out.print("hash" + ": ");
            for (RecordIdentifier recordIdentifier : recordsMap.get(hash)) {
                System.out.print(recordIdentifier.party() + "." + recordIdentifier.id() + ", ");
            }
            System.out.println();
        }
    }
}
