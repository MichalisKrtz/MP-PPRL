package other;

import db.Record;

import java.util.List;

public record RecordGroup(String blockingKeyValue, List<Record> records) {
}
