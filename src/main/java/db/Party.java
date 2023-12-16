package db;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Party {
    private List<Map<String, DynamicTypeValue>> records;

    public Party() {
        records = new ArrayList<>();
    }

    public void addRecord(Map<String, DynamicTypeValue> record) {
        records.add(record);
    }

    public void addRecords(List<Map<String, DynamicTypeValue>> records) {
        this.records.addAll(records);
    }

    public void printRecords() {
        for (Map<String, DynamicTypeValue> rec : records) {
            for (String key : rec.keySet()) {
                System.out.print(key + ": " + rec.get(key).getValue() + "\t\t");
            }
            System.out.print("\n");
        }
    }

    public void printVal(String v) {
        System.out.println(v);
    }

}
