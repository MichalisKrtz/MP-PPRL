package mp_pprl.core;

import mp_pprl.core.domain.Record;
import mp_pprl.core.encoding.BloomFilter;
import mp_pprl.core.encoding.CountingBloomFilter;
import mp_pprl.core.encoding.EncodingHandler;
import mp_pprl.soundex_based.HashedSoundexEncodedRecord;
import mp_pprl.soundex_based.NoiseDataGenerator;
import org.apache.commons.codec.language.Soundex;

import java.util.*;

public class Party {
    public final int id;
    private final List<Record> records;
    private final List<BloomFilterEncodedRecord> bloomFilterEncodedRecords;
    private final Map<String, List<BloomFilterEncodedRecord>> bloomFilterEncodedRecordGroups;
    private final String[] quasiIdentifiers;
    private final String[] blockingKeyValues;
    private final int bloomFilterLength;
    private final int numberOfHashFunctions;
    private final List<List<String>> soundexEncodedRecords;
    private final List<HashedSoundexEncodedRecord> hashedSoundexEncodedRecords;

    public Party(int id, String[] quasiIdentifiers, String[] blockingKeyValues, int bloomFilterLength, int numberOfHashFunctions) {
        this.id = id;
        records = new ArrayList<>();
        bloomFilterEncodedRecords = new ArrayList<>();
        bloomFilterEncodedRecordGroups = new HashMap<>();
        this.quasiIdentifiers = quasiIdentifiers;
        this.blockingKeyValues = blockingKeyValues;
        this.bloomFilterLength = bloomFilterLength;
        this.numberOfHashFunctions = numberOfHashFunctions;
        soundexEncodedRecords = new ArrayList<>();
        hashedSoundexEncodedRecords = new ArrayList<>();
    }


    /*Encode records to bloom filters. Set the bloom filters of the Records and the Record Identifiers*/
    public void encodeRecords(EncodingHandler encodingHandler) {
        for (Record record : records) {
            BloomFilter bf = new BloomFilter(bloomFilterLength, numberOfHashFunctions, encodingHandler);
            for (String qId : quasiIdentifiers) {
                if (qId.equals("id")) continue;
                bf.addElement(record.get(qId).getValueAsString());
            }
            bloomFilterEncodedRecords.add(new BloomFilterEncodedRecord(this, record.get("id").getValueAsString(), bf));
        }
    }

//    /*Encode records of one block to bloom filters. Set the bloom filters of the Records*/
//    public void encodeRecordsOfBlock(EncodingHandler encodingHandler, String block) {
//        bloomFilterEncodedRecordGroups.get(block).clear();
//        Soundex soundex = new Soundex();
//        for (BloomFilterEncodedRecord bloomFilterEncodedRecord : bloomFilterEncodedRecordGroups.get(block)) {
//            Optional<Record> optionalRecord = getRecordById(bloomFilterEncodedRecord.id());
//            Record record = optionalRecord.orElseThrow();
//            BloomFilter bf = bloomFilterEncodedRecord.bloomFilter();
//            for (String qId : quasiIdentifiers) {
//                if (qId.equals("id")) continue;
//                bf.addElement(record.get(qId).getValueAsString());
//            }
//        }
//
//
//        for (Record record : records) {
//            Optional<BloomFilterEncodedRecord> optionalBloomFilterEncodedRecord = getBloomFilterEncodedRecordById(record.get("id").getValueAsString());
//            BloomFilterEncodedRecord bloomFilterEncodedRecord = optionalBloomFilterEncodedRecord.orElseThrow();
//            StringBuilder soundexStringBuilder = new StringBuilder();
//            for (String bkv : blockingKeyValues) {
//                soundexStringBuilder.append(soundex.encode(record.get(bkv).getValueAsString()));
//            }
//            String soundexString = soundexStringBuilder.toString();
//
//            if (!bloomFilterEncodedRecordGroups.containsKey(soundexString)) {
//                List<BloomFilterEncodedRecord> group = new ArrayList<>();
//                group.add(bloomFilterEncodedRecord);
//                bloomFilterEncodedRecordGroups.put(soundexString, group);
//                continue;
//            }
//            bloomFilterEncodedRecordGroups.get(soundexString).add(bloomFilterEncodedRecord);
//        }
//    }

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

    public void groupBloomFilterEncodedRecordsByBlockingKeyValue() {
        Soundex soundex = new Soundex();
        for (Record record : records) {
            Optional<BloomFilterEncodedRecord> optionalBloomFilterEncodedRecord = getBloomFilterEncodedRecordById(record.get("id").getValueAsString());
            BloomFilterEncodedRecord bloomFilterEncodedRecord = optionalBloomFilterEncodedRecord.orElseThrow();
            StringBuilder soundexStringBuilder = new StringBuilder();
            for (String bkv : blockingKeyValues) {
                soundexStringBuilder.append(soundex.encode(record.get(bkv).getValueAsString()));
            }
            String soundexString = soundexStringBuilder.toString();
            if (!bloomFilterEncodedRecordGroups.containsKey(soundexString)) {
                List<BloomFilterEncodedRecord> group = new ArrayList<>();
                group.add(bloomFilterEncodedRecord);
                bloomFilterEncodedRecordGroups.put(soundexString, group);
                continue;
            }
            bloomFilterEncodedRecordGroups.get(soundexString).add(bloomFilterEncodedRecord);
        }
    }

    public void generateHashesForSoundexEncodedRecords(EncodingHandler encodingHandler) {
        for(List<String> soundexEncodedRecord : soundexEncodedRecords) {
            StringBuilder hashedData = new StringBuilder();
            // Start from 1 because 0 contains the id which should not be hashed
            for (int i = 1; i < soundexEncodedRecord.size(); i++) {
                hashedData.append(encodingHandler.hash(soundexEncodedRecord.get(i)));
            }
            // soundexEncodedRecord.getFirst() returns the recordId
            hashedSoundexEncodedRecords.add(new HashedSoundexEncodedRecord(this, soundexEncodedRecord.getFirst(), hashedData.toString()));
        }
    }

    public void truncateSoundexEncodedRecords(int charsToTruncate) {
        for (List<String> encodedFields : soundexEncodedRecords) {
            for (int i = 1; i < encodedFields.size(); i++) {
                if (!encodedFields.get(i).isEmpty()) {
                    String truncatedField = encodedFields.get(i).substring(0, encodedFields.get(i).length() - charsToTruncate);
                    encodedFields.set(i, truncatedField);
                }
            }
        }
    }

    public void generateNoise(double noisePercentage) {
        List<List<String>> noiseData = NoiseDataGenerator.generateNoiseData(soundexEncodedRecords.size(), noisePercentage);
        soundexEncodedRecords.addAll(noiseData);
    }

    public void addToCountingBloomFilter(CountingBloomFilter countingBloomFilter, String recordId) {
        for (BloomFilterEncodedRecord record : bloomFilterEncodedRecords) {
            if (recordId.equals(record.id())) {
                countingBloomFilter.addVector(record.bloomFilter().getVector());
            }
        }
    }

    public void addRecords(List<Record> records) {
        this.records.addAll(records);
    }

    public int getRecordsSize() {
        return records.size();
    }

    public List<BloomFilterEncodedRecord> getBloomFilterEncodedRecords() {
        return bloomFilterEncodedRecords;
    }

    public Map<String, List<BloomFilterEncodedRecord>> getBloomFilterEncodedRecordGroups() {
        return bloomFilterEncodedRecordGroups;
    }

    public List<HashedSoundexEncodedRecord> getHashedSoundexEncodedRecords() {
        return hashedSoundexEncodedRecords;
    }

    private Optional<BloomFilterEncodedRecord> getBloomFilterEncodedRecordById(String id) {
        for (BloomFilterEncodedRecord bloomFilterEncodedRecord : bloomFilterEncodedRecords) {
            if (bloomFilterEncodedRecord.id().equals(id)) return Optional.of(bloomFilterEncodedRecord);
        }
        return Optional.empty();
    }

    private Optional<Record> getRecordById(String id) {
        for (Record record : records) {
            if (record.get("id").getValueAsString().equals(id)) return Optional.of(record);
        }
        return Optional.empty();
    }

}
