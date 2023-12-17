import db.ByteArrayTypeValue;
import db.DynamicTypeValue;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Party {
    private final List<Map<String, DynamicTypeValue>> records;

    public Party() {
        records = new ArrayList<>();
    }

    public void addRecord(Map<String, DynamicTypeValue> record) {
        records.add(record);
    }

    public void addRecords(List<Map<String, DynamicTypeValue>> records) {
        this.records.addAll(records);
    }

    public Map<String, List<Map<String, DynamicTypeValue>>> getEncodedGroupedRecords(String[] quasiIdentifiers, String[] blockingKeyValues, int bloomFilterLength, int bloomFilterHashFunctions) {
        List<Map<String, DynamicTypeValue>> maskedRecords = maskRecords(quasiIdentifiers, bloomFilterLength, bloomFilterHashFunctions);
        Map<String, List<Map<String, DynamicTypeValue>>> groupedRecords = groupRecords(blockingKeyValues);

//        Map<String, List<Map<String, DynamicTypeValue>>> encodedGroupedRecords = new HashMap<>();
//        for (Map.Entry<String, List<Map<String, DynamicTypeValue>>> entry : groupedRecords.entrySet()) {
//            for (Map<String, DynamicTypeValue> rec : entry.getValue()) {
//
//            }
//        }
        Map<String, List<Map<String, DynamicTypeValue>>> encodedGroupedRecords = groupedRecords;

        return  encodedGroupedRecords;
    }

    private List<Map<String, DynamicTypeValue>> maskRecords(String[] quasiIdentifiers, int bloomFilterLength, int bloomFilterHashFunctions) {
        List<Map<String, DynamicTypeValue>> maskedRecords = new ArrayList<>();
        for (Map<String, DynamicTypeValue> rec : records) {
            StringBuilder sensitiveData = new StringBuilder();
            for (String qId : quasiIdentifiers) {
                sensitiveData.append(rec.get(qId).getValueAsString());
            }
            BloomFilter bf = new BloomFilter(bloomFilterLength, bloomFilterHashFunctions);
            bf.addElement(sensitiveData.toString());
            DynamicTypeValue bfCells = new ByteArrayTypeValue(bf.getCells());
            rec.put("bloomFilter",bfCells);
            maskedRecords.add(rec);
        }
        return maskedRecords;
    }

    private Map<String, List<Map<String, DynamicTypeValue>>> groupRecords(String[] blockingKeyValues) {
        Map<String, List<Map<String, DynamicTypeValue>>> groupedRecords = new HashMap<>();
        for (Map<String, DynamicTypeValue> rec : records) {
            StringBuilder blockingKeyValuesStringBuilder = new StringBuilder();
            for (String bkv : blockingKeyValues) {
                blockingKeyValuesStringBuilder.append(rec.get(bkv).getValueAsString());
            }
            String soundex = Soundex.encode(blockingKeyValuesStringBuilder.toString());
            if (!groupedRecords.containsKey(soundex)) {
                List<Map<String, DynamicTypeValue>> group = new ArrayList<>();
                group.add(rec);
                groupedRecords.put(soundex,group);
                continue;
            }
            groupedRecords.get(soundex).add(rec);
        }
        return groupedRecords;
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
