package mp_pprl.domain;

import mp_pprl.encoding.BloomFilter;

import java.util.Set;

public interface Record {
    DynamicValue get(String propertyName);

    Set<String> keySet();

    void put(String propertyName, DynamicValue propertyValue);

    void remove(String key);

    BloomFilter getBloomFilter();

    void setBloomFilter(BloomFilter bloomFilter);

    void printData();
}
