package mp_pprl.core.domain;

import mp_pprl.core.encoding.BloomFilter;

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
