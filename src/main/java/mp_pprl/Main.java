package mp_pprl;

import java.util.Arrays;

import mp_pprl.core.optimization.FloatMatrix;

public class Main {
    public static void main(String[] args) {
        System.out.println("Arguments:"+Arrays.toString(args));
        Application app = new Application(
                args[0], //dataset;authors / voters
                Integer.parseInt(args[1]), // datasetSize
                Integer.parseInt(args[2]), // errors
                Integer.parseInt(args[3]), // numParties
                args[4]					   // method
        );
        app.run(args[4]);
    }
}
