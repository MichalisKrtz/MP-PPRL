package protocols;

import db.Record;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public record Vertex(Set<Record> records) {

    public Vertex() {
        this(new HashSet<>());
    }

    public Vertex(Record record) {
        this(new HashSet<>(Collections.singletonList(record)));
    }
}
