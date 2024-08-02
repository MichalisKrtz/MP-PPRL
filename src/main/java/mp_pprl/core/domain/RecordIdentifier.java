package mp_pprl.core.domain;

import mp_pprl.core.encoding.BloomFilter;
import mp_pprl.core.Party;

public class RecordIdentifier {
    private final Party party;
    private final int id;
    private BloomFilter bloomFilter;

    public RecordIdentifier(Party party, int id, BloomFilter bloomFilter) {
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
