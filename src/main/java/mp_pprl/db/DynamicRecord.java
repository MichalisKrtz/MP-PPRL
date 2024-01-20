package mp_pprl.db;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class DynamicRecord implements Record {
    private final Map<String, DynamicValue> map;

    public DynamicRecord() {
        map = new HashMap<>();
    }

    public DynamicRecord(Map<String, DynamicValue> recordData) {
        map = recordData;
    }

    public DynamicValue get(String propertyName) {
        return map.get(propertyName);
    }

    @Override
    public Set<String> keySet() {
        return map.keySet();
    }

    public void put(String propertyName, DynamicValue propertyValue) {
        map.put(propertyName, propertyValue);
    }

    @Override
    public void printData() {
        for (String key : map.keySet()) {
            System.out.println(key + ": " + map.get(key).getValueAsString());
        }
    }
}
