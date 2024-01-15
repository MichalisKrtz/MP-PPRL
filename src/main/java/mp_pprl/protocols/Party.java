package mp_pprl.protocols;

import mp_pprl.db.Record;
import mp_pprl.db.DynamicRecord;
import mp_pprl.db.DynamicValue;
import mp_pprl.db.DynamicValueFactory;
import mp_pprl.encoding.BloomFilter;
import mp_pprl.encoding.Soundex;

import java.util.*;

public class Party {
    private final List<Record> records;
    private final String[] privateFields;
    private final String[] quasiIdentifiers;
    private final String[] blockingKeyValues;

    public Party(String[] privateFields, String[] quasiIdentifiers, String[] blockingKeyValues) {
        this.privateFields = privateFields.clone();
        this.quasiIdentifiers = quasiIdentifiers.clone();
        this.blockingKeyValues = blockingKeyValues.clone();
        records = new ArrayList<>();
    }

    public void addRecords(List<Record> records) {
        this.records.addAll(records);
    }

    public Map<String, List<Record>> shareRecords(int bloomFilterLength, int bloomFilterHashFunctions) {
        generateBloomFilters(bloomFilterLength, bloomFilterHashFunctions);
        Map<String, List<Record>> groupedMaskedRecords = groupRecords();

        return removePrivateFieldsFromGroupedRecords(groupedMaskedRecords);
    }

    private void generateBloomFilters(int bloomFilterLength, int bloomFilterHashFunctions) {
        for (Record rec : records) {
            StringBuilder sensitiveData = new StringBuilder();
            for (String qId : quasiIdentifiers) {
                sensitiveData.append(rec.get(qId).getValueAsString());
            }
            BloomFilter bf = new BloomFilter(bloomFilterLength, bloomFilterHashFunctions);
            bf.addElement(sensitiveData.toString());
            DynamicValue bfCells = DynamicValueFactory.createDynamicValue("BYTE_ARRAY", bf.getCells());
            rec.put("bloom_filter", bfCells);
        }
    }

    private Map<String, List<Record>> groupRecords() {
        Map<String, List<Record>> recordGroups = new HashMap<>();
        for (Record rec : records) {
            StringBuilder soundexStringBuilder = new StringBuilder();
            for (String bkv : blockingKeyValues) {
                soundexStringBuilder.append(Soundex.encode(rec.get(bkv).getValueAsString()));
            }
            String soundex = soundexStringBuilder.toString();
            if (!recordGroups.containsKey(soundex)) {
                List<Record> group = new ArrayList<>();
                group.add(rec);
                recordGroups.put(soundex, group);
                continue;
            }
            recordGroups.get(soundex).add(rec);
        }
        return recordGroups;
    }

    private Map<String, List<Record>> removePrivateFieldsFromGroupedRecords(Map<String, List<Record>> groupedRecords) {
        Map<String, List<Record>> sharableRecordGroups = new HashMap<>();
        for (Map.Entry<String, List<Record>> entry : groupedRecords.entrySet()) {
            String newSoundex = entry.getKey();
            List<Record> newGroup = new ArrayList<>();
            for (Record recordsGroup : entry.getValue()) {
                Record newRecord = new DynamicRecord();
                for (String field : recordsGroup.keySet()) {
                    if (!Arrays.asList(privateFields).contains(field)) {
                        newRecord.put(field, recordsGroup.get(field));
                    }
                }
                newGroup.add(newRecord);
            }
            sharableRecordGroups.put(newSoundex, newGroup);
        }
        return sharableRecordGroups;
    }
}
