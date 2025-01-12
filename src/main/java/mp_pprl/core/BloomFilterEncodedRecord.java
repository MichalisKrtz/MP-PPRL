package mp_pprl.core;

import mp_pprl.core.encoding.BloomFilter;

public class BloomFilterEncodedRecord {
    Party party;
    String id;
    BloomFilter bloomFilter;

    public BloomFilterEncodedRecord(Party party, String id, BloomFilter bloomFilter) {
        this.party = party;
        this.id = id;
        this.bloomFilter = bloomFilter;
    }

    public String getId() {
        return id;
    }

    public Party getParty() {
        return party;
    }

    public BloomFilter getBloomFilter() {
        return bloomFilter;
    }

    public void setBloomFilter(BloomFilter bloomFilter) {
        this.bloomFilter = bloomFilter;
    }

    @Override
    public String toString() {
        return "RecordIdentifier{" +
                "party=" + party +
                ", id=" + id +
                ", bloomFilter=" + bloomFilter +
                '}';
    }
}
