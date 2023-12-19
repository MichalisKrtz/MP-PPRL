package repositories;

import db.DynamicValue;
import db.PartyDAO;
import db.Record;

import java.util.List;
import java.util.Map;

public class PartiesRepository {
    public static List<Record> selectAll(String dbPath) {
        PartyDAO partyDao = new PartyDAO(dbPath);
        return partyDao.selectAll();
    }
}
