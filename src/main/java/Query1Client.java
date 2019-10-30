import com.hazelcast.client.HazelcastClient;
import com.hazelcast.client.config.ClientConfig;
import com.hazelcast.config.*;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.ICompletableFuture;
import com.hazelcast.core.IList;
import com.hazelcast.mapreduce.*;
import org.apache.commons.lang3.tuple.ImmutableTriple;
import org.apache.commons.lang3.tuple.Triple;


import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

public class Query1Client {
    private static class MoveMapper implements Mapper<String, Move, String, Integer> {
        @Override
        public void map(String s, Move move, Context<String, Integer> context) {
            String airportOaci = move.moveType == MoveType.Takeoff ? move.originOaci : move.destinationOaci;

            context.emit(airportOaci, 1);
        }
    } // { oaci: [EZ, EZ, ASF] }
    // { EZ: [1, 1, 1, 1, 1], ASF: [1] }


    private static class MoveCountReducerFactory implements ReducerFactory<String, Integer, Integer> {
        @Override
        public Reducer<Integer, Integer> newReducer(String oaci) {
            return new MoveCountReducer();
        }

        class MoveCountReducer extends Reducer<Integer, Integer> {
            private volatile int moves;

            @Override
            public void beginReduce() {
                moves = 0;
            }

            @Override
            public void reduce(Integer value) {
                moves += value.intValue();
            }

            @Override
            public Integer finalizeReduce() {
                return moves;
            }
        }
    }

    private static class MoveCollator implements Collator<Map.Entry<String, Integer>, List<Triple<String, String, Integer>>> {
        private List<Airport> airports;

        public MoveCollator(List<Airport> airports) {
            this.airports = airports;
        }

        @Override
        public List<Triple<String, String, Integer>> collate(Iterable<Map.Entry<String, Integer>> values) {
            List<Triple<String, String, Integer>> results = new ArrayList<>();

            for (Map.Entry<String, Integer> entry : values) {
                String name = airports.stream().filter(airport -> airport.oaci.equals(entry.getKey())).map(airport -> airport.name).findFirst().orElse(null);

                if (name == null) {
                    continue;
                }

                Triple<String, String, Integer> triple = new ImmutableTriple<>(entry.getKey(), name, entry.getValue());

                results.add(triple);
            }

            results.sort((triple1, triple2) -> {
                int res1 = triple2.getRight() - triple1.getRight();

                if (res1 != 0) {
                    return res1;
                }

                return triple1.getLeft().compareTo(triple2.getLeft());
            });
            return results;
        }
    }

    public static void main(String[] args) throws ExecutionException, InterruptedException {
        final ClientConfig clientConfig = new ClientConfig();
        clientConfig.getNetworkConfig().addAddress("127.0.0.1:5701");
        System.out.println("Connecting to remote Hazelcast node");
        final HazelcastInstance hazelClient = HazelcastClient.newHazelcastClient(clientConfig);
        System.out.println("Done");

        JobTracker jobTracker = hazelClient.getJobTracker("move-count");
        IList<Move> iMoves = hazelClient.getList("g6-moves");
        IList<Airport> iAirports = hazelClient.getList("g6-airports");
        iAirports.forEach(System.out::println);

        final KeyValueSource<String, Move> source = KeyValueSource.fromList(iMoves);

        Job<String, Move> job = jobTracker.newJob(source);
        JobCompletableFuture<List<Triple<String, String, Integer>>> future =
                job.mapper(new MoveMapper())
                        .reducer(new MoveCountReducerFactory())
                        .submit(new MoveCollator(iAirports));

        List<Triple<String, String, Integer>> queryResults = future.get();
        System.out.println("thing finished");

        System.out.println("Printing results to file:");
        serializeQuery(queryResults);
        System.out.println("Done");

    }

    public static void serializeQuery(List<Triple<String, String, Integer>> queryResults) {
        String[] headers = {"OACI", "Denominación", "Movimientos"};
        List<String[]> lines = new ArrayList<>();

        lines.add(headers);
        for (Triple<String, String, Integer> triple : queryResults) {
            String[] line = {triple.getLeft(), triple.getMiddle(), triple.getRight().toString()};
            lines.add(line);
        }

        Output.print("./results/query1.csv", lines);
    }
}
