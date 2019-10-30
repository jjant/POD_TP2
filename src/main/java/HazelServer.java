import com.hazelcast.config.Config;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IList;

import java.io.IOException;
import java.util.List;

public class HazelServer {
    public static void main(String[] args) throws IOException {
        final Config config = new Config();
        final HazelcastInstance hazelServer = Hazelcast.newHazelcastInstance(config);

        System.out.println("Initializing move list");
        IList<Move> iMoves = hazelServer.getList("g6-moves");
        iMoves.clear();
        List<Move> moves = Parse.parseMoves();
        iMoves.addAll(moves);
        System.out.println("Done initializing move list");


        System.out.println("Initializing airport list");
        IList<Airport> iAirports = hazelServer.getList("g6-airports");
        iAirports.clear();
        List<Airport> airports = Parse.parseAirports();
        iAirports.addAll(airports);
        System.out.println("Done initializing airport list");

        System.out.println("Server ready for shit");
    }
}
