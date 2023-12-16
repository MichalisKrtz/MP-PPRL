import db.DynamicTypeValue;
import db.Party;
import db.PartyDAO;
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
        p1.printRecords();


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
