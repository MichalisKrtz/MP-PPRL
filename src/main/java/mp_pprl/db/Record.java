package mp_pprl.db;

import java.util.Set;

public interface Record {
    DynamicValue get(String propertyName);

    Set<String> keySet();

    void put(String propertyName, DynamicValue propertyValue);

    void printData();
}
