package mp_pprl.core;

import mp_pprl.core.domain.Record;
import mp_pprl.core.encoding.BloomFilter;
import mp_pprl.core.encoding.CountingBloomFilter;
import mp_pprl.core.encoding.EncodingHandler;
import mp_pprl.soundex_based.NoiseDataGenerator;
import org.apache.commons.codec.language.Soundex;

import java.util.*;

public class Party {
    private final List<Record> records;
    private final List<BloomFilterEncodedRecord> bloomFilterEncodedRecords;
    private Map<String, List<BloomFilterEncodedRecord>> recordIdentifierGroups;
    private final String[] quasiIdentifiers;
    private final String[] blockingKeyValues;
    private final int bloomFilterLength;
    private final int numberOfHashFunctions;
    private final List<List<String>> soundexEncodedRecords;
    private final List<HashedSoundexEncodedRecord> hashedSoundexEncodedRecords;

    public Party(String[] quasiIdentifiers, String[] blockingKeyValues, int bloomFilterLength, int numberOfHashFunctions) {
        records = new ArrayList<>();
        bloomFilterEncodedRecords = new ArrayList<>();
        this.quasiIdentifiers = quasiIdentifiers;
        this.blockingKeyValues = blockingKeyValues;
        this.bloomFilterLength = bloomFilterLength;
        this.numberOfHashFunctions = numberOfHashFunctions;
        generateRecordGroups();
        soundexEncodedRecords = new ArrayList<>();
        hashedSoundexEncodedRecords = new ArrayList<>();
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
            bloomFilterEncodedRecords.add(new BloomFilterEncodedRecord(this, i, bf));
            records.get(i).setBloomFilter(bf);
        }
    }

    /*Encode records of one block to bloom filters. Set the bloom filters of the Records*/
    public void encodeRecordsOfBlock(EncodingHandler encodingHandler, String block) {
        for (BloomFilterEncodedRecord bloomFilterEncodedRecord : recordIdentifierGroups.get(block)) {
            int recordIndex = bloomFilterEncodedRecord.getId();
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

    public List<BloomFilterEncodedRecord> getRecordIdentifiers() {
        return bloomFilterEncodedRecords;
    }

    public Map<String, List<BloomFilterEncodedRecord>> getRecordIdentifierGroups() {
        return recordIdentifierGroups;
    }

    public void encodeRecordsWithSoundex() {
        Soundex soundex = new Soundex();
        for (Record record : records) {
            List<String> encodedFields = new ArrayList<>();
            for (String qId : quasiIdentifiers) {
                if (qId.equals("id")) {
                    encodedFields.add(record.get(qId).getValueAsString());
                    continue;
                }
                encodedFields.add(soundex.encode((record.get(qId).getValueAsString())));
            }
            soundexEncodedRecords.add(encodedFields);
        }
    }

    public void truncateSoundexEncodedRecords() {
        for (List<String> encodedFields : soundexEncodedRecords) {
            for (int i = 1; i < encodedFields.size(); i++) {
                if (!encodedFields.get(i).isEmpty()) {
                    String truncatedField = encodedFields.get(i).substring(0, encodedFields.get(i).length() - 1);
                    encodedFields.set(i, truncatedField);
                }
            }
        }
    }

    public void generateHashesForSoundexEncodedRecords(EncodingHandler encodingHandler) {
        for(List<String> soundexEncodedRecord : soundexEncodedRecords) {
            StringBuilder hashedData = new StringBuilder();
            // Start from 1 because 0 contains the id which should not be hashed
            for (int i = 1; i < soundexEncodedRecord.size(); i++) {
                hashedData.append(encodingHandler.hash(soundexEncodedRecord.get(i)));
            }
            hashedSoundexEncodedRecords.add(new HashedSoundexEncodedRecord(hashedData.toString(), this, soundexEncodedRecord.getFirst()));
        }
    }

    public void generateNoise(double noisePercentage) {
        List<List<String>> noiseData = NoiseDataGenerator.generateNoiseData(soundexEncodedRecords.size(), noisePercentage);
        soundexEncodedRecords.addAll(noiseData);
    }

    public List<HashedSoundexEncodedRecord> getHashedSoundexEncodedRecords() {
        return hashedSoundexEncodedRecords;
    }

    private Map<String, List<BloomFilterEncodedRecord>> groupRecordIdentifiersByBlockingKeyValue() {
        Map<String, List<BloomFilterEncodedRecord>> recordGroups = new HashMap<>();
        Soundex soundex = new Soundex();
        for (int i = 0; i < records.size(); i++) {
            Optional<BloomFilterEncodedRecord> optionalRecordIdentifier = getRecordIdentifierById(i);
            BloomFilterEncodedRecord bloomFilterEncodedRecord = optionalRecordIdentifier.orElseThrow();
            StringBuilder soundexStringBuilder = new StringBuilder();
            for (String bkv : blockingKeyValues) {
                soundexStringBuilder.append(soundex.encode(records.get(i).get(bkv).getValueAsString()));
            }
            String soundexString = soundexStringBuilder.toString();
            if (!recordGroups.containsKey(soundexString)) {
                List<BloomFilterEncodedRecord> group = new ArrayList<>();
                group.add(bloomFilterEncodedRecord);
                recordGroups.put(soundexString, group);
                continue;
            }
            recordGroups.get(soundexString).add(bloomFilterEncodedRecord);
        }
        return recordGroups;
    }

    private Optional<BloomFilterEncodedRecord> getRecordIdentifierById(int id) {
        for (BloomFilterEncodedRecord bloomFilterEncodedRecord : bloomFilterEncodedRecords) {
            if (bloomFilterEncodedRecord.getId() == id) return Optional.of(bloomFilterEncodedRecord);
        }
        return Optional.empty();
    }

}
