package repositories;

import db.DynamicTypeValue;
import db.PartyDAO;

import java.util.List;
import java.util.Map;

public class PartiesRepository {
    public static List<Map<String, DynamicTypeValue>> selectAll(String dbPath) {
        PartyDAO partyDao = new PartyDAO(dbPath);
        return partyDao.selectAll();
    }
}
