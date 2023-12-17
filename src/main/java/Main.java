import db.DynamicTypeValue;
import repositories.PartiesRepository;

import java.util.List;
import java.util.Map;

public class Main {
    public static void main(String[] args) {
        System.out.println("Main function of the MP-PPRL project");
        String dbPath = "C:\\Users\\Michael\\Desktop\\Thesis\\Dev\\db1.db";

        String[] quasiIdentifiers = {"first_name", "last_name"};
        String[] blockingKeyValues = {"last_name"};
        Party p1 = new Party(blockingKeyValues ,quasiIdentifiers, blockingKeyValues);
        p1.printBKV();
        blockingKeyValues[0] = "last_last_last";
        System.out.println("Main bkvs: "+blockingKeyValues[0]);
        p1.printBKV();
        List<Map<String, DynamicTypeValue>> newRecords = PartiesRepository.selectAll(dbPath);
        p1.addRecords(newRecords);
        Map<String, List<Map<String, DynamicTypeValue>>> p1Records = p1.shareRecords(10, 1);

        for (Map.Entry<String, List<Map<String, DynamicTypeValue>>> entry : p1Records.entrySet()) {
            System.out.println(entry.getKey());
            for (Map<String, DynamicTypeValue> rec : entry.getValue()) {
                for (String key : rec.keySet()) {
                    System.out.print(key + ": " + rec.get(key).getValueAsString() + "\t");
                }
                System.out.print("\n");
            }
        }



//        String name1 = "Michael";
//        String name2 = "Micharl";
//
//        BloomFilter bf1 = new BloomFilter(10, 2);
//        BloomFilter bf2 = new BloomFilter(10, 2);
//
//        bf1.addElement(name1);
//        bf2.addElement(name2);
//
//        bf1.printCells();
//        bf2.printCells();
    }
}
