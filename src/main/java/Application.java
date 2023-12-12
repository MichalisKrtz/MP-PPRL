import db.PartyDAO;
import protocols.EarlyMappingClusteringProtocol;

public class Application {

    public static void run() {
        System.out.println("Application run function");
    }

    public static void runExample() {
        System.out.println("Main function of the MP-PPRL project");

        PartyDAO daoObject = new PartyDAO("C:\\Users\\Michael\\Desktop\\Thesis\\Dev\\dm_root_v3.db");
        daoObject.selectFromValues();
    }
}
