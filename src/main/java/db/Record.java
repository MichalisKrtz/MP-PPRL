package db;

import java.util.Set;

public interface Record {
    public DynamicValue get(String propertyName);
    public Set<String> keySet();

    public void put(String propertyName, DynamicValue propertyValue);

    void printData();
}
