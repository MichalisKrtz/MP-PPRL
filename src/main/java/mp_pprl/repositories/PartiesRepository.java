package mp_pprl.repositories;

import mp_pprl.db.RecordDAO;
import mp_pprl.db.Record;

import java.util.List;

public class PartiesRepository {
    public static List<Record> selectAll(String dbPath) {
        RecordDAO partyDao = new RecordDAO(dbPath);
        return partyDao.selectAll();
    }
}
