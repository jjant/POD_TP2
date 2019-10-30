import com.hazelcast.config.*;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.ICompletableFuture;
import com.hazelcast.core.IList;
import com.hazelcast.mapreduce.*;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

public class HazelServer {
    private static class MoveMapper implements Mapper<String, Move, Move, String> {
        @Override
        public void map(String s, Move move, Context<Move, String> context) {
            context.emit(move, move.destinationOaci);
        }
    }

    private static class MoveReducerFactory implements ReducerFactory<Move, String, String> {

        @Override
        public Reducer<String, String> newReducer(Move move) {
            return new WordCountReducer();
        }

        class WordCountReducer extends Reducer<String, String> {
            @Override
            public void beginReduce() {
            }

            @Override
            public void reduce(String value) {
            }

            @Override
            public String finalizeReduce() {
                return "hey";
            }
        }
    }

    public static void main(String[] args) throws IOException, ExecutionException, InterruptedException {
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
