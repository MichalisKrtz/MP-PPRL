package mp_pprl.protocols;

import mp_pprl.domain.Record;
import mp_pprl.domain.RecordIdentifier;
import mp_pprl.encoding.BloomFilter;
import mp_pprl.encoding.CountingBloomFilter;
import mp_pprl.encoding.EncodingHandler;
import mp_pprl.encoding.Soundex;

import java.util.*;

public class Party {
    private final List<Record> records;
    private final List<RecordIdentifier> recordIdentifiers;
    private Map<String, List<RecordIdentifier>> recordIdentifierGroups;
    private final String[] quasiIdentifiers;
    private final String[] blockingKeyValues;
    private final int bloomFilterLength;
    private final int numberOfHashFunctions;

    public Party(String[] quasiIdentifiers, String[] blockingKeyValues, int bloomFilterLength, int numberOfHashFunctions) {
        records = new ArrayList<>();
        recordIdentifiers = new ArrayList<>();
        this.quasiIdentifiers = quasiIdentifiers;
        this.blockingKeyValues = blockingKeyValues;
        this.bloomFilterLength = bloomFilterLength;
        this.numberOfHashFunctions = numberOfHashFunctions;
        generateRecordGroups();
    }

    public void generateRecordGroups() {
        recordIdentifierGroups = groupRecordIdentifiersByBlockingKeyValue();
    }

    /*Encode records to bloom filters. Set the bloom filters of the Records and the Record Identifiers*/
    public void encodeRecords(EncodingHandler encodingHandler) {
        for (int i = 0; i < records.size(); i++) {
            BloomFilter bf = new BloomFilter(bloomFilterLength, numberOfHashFunctions, encodingHandler);
            for (String qId : quasiIdentifiers) {
                bf.addElement(records.get(i).get(qId).getValueAsString());
            }
            recordIdentifiers.add(new RecordIdentifier(this, i, bf));
            records.get(i).setBloomFilter(bf);
        }
    }

    /*Encode records of one block to bloom filters. Set the bloom filters of the Records*/
    public void encodeRecordsOfBlock(EncodingHandler encodingHandler, String block) {
        for (RecordIdentifier recordIdentifier : recordIdentifierGroups.get(block)) {
            int recordIndex = recordIdentifier.getId();
            BloomFilter bf = new BloomFilter(bloomFilterLength, numberOfHashFunctions, encodingHandler);
            for (String qId : quasiIdentifiers) {
                bf.addElement(records.get(recordIndex).get(qId).getValueAsString());
            }
            records.get(recordIndex).setBloomFilter(bf);
        }
    }

    public void addToCountingBloomFilter(CountingBloomFilter countingBloomFilter, int recordId) {
        countingBloomFilter.addVector(records.get(recordId).getBloomFilter().getVector());
    }

    public void addRecords(List<Record> records) {
        this.records.addAll(records);
    }

    public int getRecordsSize() {
        return records.size();
    }

    public Map<String, List<RecordIdentifier>> getRecordIdentifierGroups() {
        return recordIdentifierGroups;
    }

    private Map<String, List<RecordIdentifier>> groupRecordIdentifiersByBlockingKeyValue() {
        Map<String, List<RecordIdentifier>> recordGroups = new HashMap<>();
        for (int i = 0; i < records.size(); i++) {
            Optional<RecordIdentifier> optionalRecordIdentifier = getRecordIdentifierById(i);
            RecordIdentifier recordIdentifier = optionalRecordIdentifier.orElseThrow();
            StringBuilder soundexStringBuilder = new StringBuilder();
            for (String bkv : blockingKeyValues) {
                soundexStringBuilder.append(Soundex.encode(records.get(i).get(bkv).getValueAsString()));
            }
            String soundex = soundexStringBuilder.toString();
            if (!recordGroups.containsKey(soundex)) {
                List<RecordIdentifier> group = new ArrayList<>();
                group.add(recordIdentifier);
                recordGroups.put(soundex, group);
                continue;
            }
            recordGroups.get(soundex).add(recordIdentifier);
        }
        return recordGroups;
    }

    private Optional<RecordIdentifier> getRecordIdentifierById(int id) {
        for (RecordIdentifier recordIdentifier : recordIdentifiers) {
            if (recordIdentifier.getId() == id) return Optional.of(recordIdentifier);
        }
        return Optional.empty();
    }

}
