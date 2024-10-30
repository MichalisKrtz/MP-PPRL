package mp_pprl.core;

import java.util.List;

public class HashedSoundexEncodedRecord {
    private final String hash;
    private final Party party;
    private final String id;

    public HashedSoundexEncodedRecord(String hash, Party party, String id) {
        this.hash = hash;
        this.party = party;
        this.id = id;
    }

    public String getHash() {
        return hash;
    }

    public Party getParty() {
        return party;
    }

    public String getId() {
        return id;
    }
}
