package mp_pprl.core.domain;

import mp_pprl.core.encoding.BloomFilter;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class DynamicRecord implements Record {
    private final Map<String, DynamicValue> map;
    private BloomFilter bloomFilter;

    public DynamicRecord() {
        map = new HashMap<>();
    }

    public DynamicRecord(Map<String, DynamicValue> recordData) {
        map = recordData;
    }

    @Override
    public DynamicValue get(String propertyName) {
        return map.get(propertyName);
    }

    @Override
    public void put(String propertyName, DynamicValue propertyValue) {
        map.put(propertyName, propertyValue);
    }

    @Override
    public void remove(String key) {
        map.remove(key);
    }

    @Override
    public Set<String> keySet() {
        return map.keySet();

    }

    @Override
    public void printData() {
        for (String key : map.keySet()) {
            System.out.println(key + ": " + map.get(key).getValueAsString());
        }
    }

    @Override
    public BloomFilter getBloomFilter() {
        return bloomFilter;
    }

    @Override
    public void setBloomFilter(BloomFilter bloomFilter) {
        this.bloomFilter = bloomFilter;
    }

}
