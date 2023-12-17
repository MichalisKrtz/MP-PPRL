import db.DynamicTypeValue;
import repositories.PartiesRepository;

import java.util.List;
import java.util.Map;

public class Main {
    public static void main(String[] args) {
        System.out.println("Main function of the MP-PPRL project");
        String dbPath = "C:\\Users\\Michael\\Desktop\\Thesis\\Dev\\db4.db";

        Party p1 = new Party();
        List<Map<String, DynamicTypeValue>> newRecords = PartiesRepository.selectAll(dbPath);
        p1.addRecords(newRecords);
        String[] quasiIdentifiers = new String[2];
        quasiIdentifiers[0] = "first_name";
        quasiIdentifiers[1] = "last_name";
        String[] blockingKeyValues = new String[1];
        blockingKeyValues[0] = "last_name";
        Map<String, List<Map<String, DynamicTypeValue>>> p1Records =
                p1.getEncodedGroupedRecords(quasiIdentifiers,
                blockingKeyValues,
                10,
                2
        );

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
