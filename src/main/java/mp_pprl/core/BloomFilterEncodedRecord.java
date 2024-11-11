package mp_pprl.core;

import mp_pprl.core.encoding.BloomFilter;

public record BloomFilterEncodedRecord(Party party, String id, BloomFilter bloomFilter) {

    @Override
    public String toString() {
        return "RecordIdentifier{" +
                "party=" + party +
                ", id=" + id +
                ", bloomFilter=" + bloomFilter +
                '}';
    }
}
