package mp_pprl.core;

import mp_pprl.core.encoding.BloomFilter;

public class BloomFilterEncodedRecord {
    private final Party party;
    private final int id;
    private final BloomFilter bloomFilter;

    public BloomFilterEncodedRecord(Party party, int id, BloomFilter bloomFilter) {
        this.party = party;
        this.id = id;
        this.bloomFilter = bloomFilter;
    }

    public Party getParty() {
        return party;
    }

    public int getId() {
        return id;
    }

    public BloomFilter getBloomFilter() {
        return bloomFilter;
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
