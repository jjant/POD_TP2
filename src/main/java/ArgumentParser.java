import org.slf4j.Logger;

import java.util.Arrays;
import java.util.List;

public class ArgumentParser {
    public static List<String> getAddresses(Logger logger) {
        String addresses = System.getProperty("addresses");

        if (addresses == null) {
            logger.error("Addresses property missing. Try running with the following option: `-Daddresses='10.6.0.1:5701;10.6.0.2:5701`");
            System.exit(1);
        }

        return Arrays.asList(addresses.replace("\'", "").split(";"));
    }

    public static String getInPath(Logger logger) {
        String inPath = System.getProperty("inPath");

        if (inPath == null) {
            logger.error("inPath property missing. Try running with the following option: `-DinPath=/afs/it.itba.edu.ar/pub/pod/`");
            System.exit(1);
        }

        return inPath.replace("\'", "");
    }


    public static String getOutPath(Logger logger) {
        String outPath = System.getProperty("outPath");

        if (outPath == null) {
            logger.error("OutPath property missing. Try running with the following option: `-DoutPath=/afs/it.itba.edu.ar/pub/pod-write/gX/`");
            System.exit(1);
        }

        return outPath.replace("\'", "");
    }

    public static int getN(Logger logger) {
        String nString = System.getProperty("n");

        if (nString == null) {
            logger.error("N property missing. Try running with the following option: `-Dn=5`");
            System.exit(1);
        }

        int N = 0;

        try {
            N = Integer.parseInt(nString.replace("\'", ""), 10);
        } catch (NumberFormatException exception) {
            logger.error("N property found, but it's not a valid number (found `" + nString + "`). Try running with `-Dn=5`.");
            System.exit(1);
        }

        return N;
    }

    public static String getOaci(Logger logger) {
        String oaci = System.getProperty("oaci");

        if (oaci == null) {
            logger.error("OACI property missing. Try running with the following option: `-Doaci=SAEZ`");
            System.exit(1);
        }

        return oaci.replace("\'", "");
    }
}
