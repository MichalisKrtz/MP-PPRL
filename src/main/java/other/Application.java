package other;

import db.DynamicValue;
import db.Record;
import protocols.EarlyMappingClusteringProtocol;
import repositories.PartiesRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Application {
    public static final int BLOOM_FILTER_LENGTH = 5;
    public static final int NUMBER_OF_HASH_FUNCTIONS = 1;

    public static void run() {
        System.out.println("other.Application running...");

        String db_one_path = "C:\\Users\\Michael\\Desktop\\Thesis\\Dev\\dataset_one.db";
        String db_two_path = "C:\\Users\\Michael\\Desktop\\Thesis\\Dev\\dataset_two.db";
        String db_three_path = "C:\\Users\\Michael\\Desktop\\Thesis\\Dev\\dataset_three.db";

        String[] privateFields = {"first_name", "last_name"};
        String[] quasiIdentifiers = {"first_name", "last_name"};
        String[] blockingKeyValues = {"last_name"};

        System.out.println("Getting records from the databases...");
        List<Record> partyOneRecords = PartiesRepository.selectAll(db_one_path);
        List<Record> partyTwoRecords = PartiesRepository.selectAll(db_two_path);
        List<Record> partyThreeRecords = PartiesRepository.selectAll(db_three_path);

        Party partyOne = new Party(privateFields, quasiIdentifiers, blockingKeyValues);
        Party partyTwo = new Party(privateFields, quasiIdentifiers, blockingKeyValues);
        Party partyThree = new Party(privateFields, quasiIdentifiers, blockingKeyValues);

        System.out.println("Adding records to parties...");
        partyOne.addRecords(partyOneRecords);
        partyTwo.addRecords(partyTwoRecords);
        partyThree.addRecords(partyThreeRecords);

        System.out.println("Parties sharing records...");
        Map<String, List<Record>> partyOneSharedRecords = partyOne.shareRecords(BLOOM_FILTER_LENGTH, NUMBER_OF_HASH_FUNCTIONS);
        Map<String, List<Record>> partyTwoSharedRecords = partyTwo.shareRecords(BLOOM_FILTER_LENGTH, NUMBER_OF_HASH_FUNCTIONS);
        Map<String, List<Record>> partyThreeSharedRecords = partyThree.shareRecords(BLOOM_FILTER_LENGTH, NUMBER_OF_HASH_FUNCTIONS);

//        printPartyRecords(partyOneSharedRecords);
//        printPartyRecords(partyTwoSharedRecords);
//        printPartyRecords(partyThreeSharedRecords);
        List<Map<String, List<Record>>> sharedRecords = new ArrayList<>();
        sharedRecords.add(partyOneSharedRecords);
        sharedRecords.add(partyTwoSharedRecords);
        sharedRecords.add(partyThreeSharedRecords);

        EarlyMappingClusteringProtocol EMap = new EarlyMappingClusteringProtocol(sharedRecords);
        EMap.run(0.85, 2);

        System.out.println("other.Application finished successfully");
    }

    public static void printPartyRecords(Map<String, List<Record>> partyRecords) {
//        for (Map.Entry<String, List<Map<String, DynamicTypeValue>>> entry : partyRecords.entrySet()) {
//            System.out.println(entry.getKey());
//            for (Map<String, DynamicTypeValue> rec : entry.getValue()) {
////                for (String key : rec.keySet()) {
////                    System.out.print(key + ": " + rec.get(key).getValueAsString() + "\t");
////                }
//                System.out.print("\n");
//            }
//        }
        System.out.println(partyRecords.size());
    }
}
