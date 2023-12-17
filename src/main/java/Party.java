import db.ByteArrayTypeValue;
import db.DynamicTypeValue;

import java.util.*;

public class Party {
    private final List<Map<String, DynamicTypeValue>> records;
    private final String[] privateFields;
    private final String[] quasiIdentifiers;
    private final String[] blockingKeyValues;
    public Party(String[] privateFields ,String[] quasiIdentifiers, String[] blockingKeyValues) {
        this.privateFields = privateFields.clone();
        this.quasiIdentifiers = quasiIdentifiers.clone();
        this.blockingKeyValues = blockingKeyValues.clone();
        records = new ArrayList<>();
    }

    public void printBKV() {
        System.out.println(Arrays.toString(blockingKeyValues));
    }

    public void addRecord(Map<String, DynamicTypeValue> record) {
        records.add(record);
    }

    public void addRecords(List<Map<String, DynamicTypeValue>> records) {
        this.records.addAll(records);
    }

    public Map<String, List<Map<String, DynamicTypeValue>>> shareRecords(int bloomFilterLength, int bloomFilterHashFunctions) {
        generateBloomFilters(bloomFilterLength, bloomFilterHashFunctions);
        Map<String, List<Map<String, DynamicTypeValue>>> groupedMaskedRecords = groupRecords();
//        Map<String, List<Map<String, DynamicTypeValue>>> encodedGroupedRecords = groupedMaskedRecords;

        return removePrivateFieldsFromGroupedRecords(groupedMaskedRecords);
    }

    private void generateBloomFilters(int bloomFilterLength, int bloomFilterHashFunctions) {
        for (Map<String, DynamicTypeValue> rec : records) {
            StringBuilder sensitiveData = new StringBuilder();
            for (String qId : quasiIdentifiers) {
                sensitiveData.append(rec.get(qId).getValueAsString());
            }
            BloomFilter bf = new BloomFilter(bloomFilterLength, bloomFilterHashFunctions);
            bf.addElement(sensitiveData.toString());
            DynamicTypeValue bfCells = new ByteArrayTypeValue(bf.getCells());
            rec.put("bloomFilter",bfCells);
        }
    }

    private Map<String, List<Map<String, DynamicTypeValue>>> groupRecords() {
        Map<String, List<Map<String, DynamicTypeValue>>> recordGroups = new HashMap<>();
        for (Map<String, DynamicTypeValue> rec : records) {
            StringBuilder blockingKeyValuesStringBuilder = new StringBuilder();
            for (String bkv : blockingKeyValues) {
                blockingKeyValuesStringBuilder.append(rec.get(bkv).getValueAsString());
            }
            String soundex = Soundex.encode(blockingKeyValuesStringBuilder.toString());
            if (!recordGroups.containsKey(soundex)) {
                List<Map<String, DynamicTypeValue>> group = new ArrayList<>();
                group.add(rec);
                recordGroups.put(soundex, group);
                continue;
            }
            recordGroups.get(soundex).add(rec);
        }
        return recordGroups;
    }

    private Map<String, List<Map<String, DynamicTypeValue>>> removePrivateFieldsFromGroupedRecords(Map<String, List<Map<String, DynamicTypeValue>>> groupedRecords) {
        Map<String, List<Map<String, DynamicTypeValue>>> sharableRecordGroups = new HashMap<>();
        for (Map.Entry<String, List<Map<String, DynamicTypeValue>>> entry : groupedRecords.entrySet()) {
            String newSoundex = entry.getKey();
            List<Map<String, DynamicTypeValue>> newGroup = new ArrayList<>();
            for (Map<String, DynamicTypeValue> recordsGroup : entry.getValue()) {
                Map<String, DynamicTypeValue> newRecord = new HashMap<>();
                for (String field : recordsGroup.keySet()) {
                    if (!Arrays.asList(privateFields).contains(field)) {
                        newRecord.put(field, recordsGroup.get(field));
                    }
                }
                newGroup.add(newRecord);
            }
            sharableRecordGroups.put(newSoundex, newGroup);
        }
        return  sharableRecordGroups;
    }

    private void printRecords() {
        for (Map<String, DynamicTypeValue> rec : records) {
            for (String key : rec.keySet()) {
                System.out.print(key + ": " + rec.get(key).getValueAsString() + "\t");
            }
            System.out.print("\n");
        }
    }
}
