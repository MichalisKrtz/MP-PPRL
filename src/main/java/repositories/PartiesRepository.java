package repositories;

import db.RecordDAO;
import db.Record;

import java.util.List;

public class PartiesRepository {
    public static List<Record> selectAll(String dbPath) {
        RecordDAO partyDao = new RecordDAO(dbPath);
        return partyDao.selectAll();
    }
}
