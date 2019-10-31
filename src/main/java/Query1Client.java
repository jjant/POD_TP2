import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IList;
import com.hazelcast.mapreduce.*;
import org.apache.commons.lang3.tuple.ImmutableTriple;
import org.apache.commons.lang3.tuple.Triple;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicInteger;

public class Query1Client {
    private static final Logger logger = LoggerFactory.getLogger(Query1Client.class);

    private static class MoveMapper implements Mapper<String, Move, String, Integer> {
        @Override
        public void map(String s, Move move, Context<String, Integer> context) {
            String airportOaci = move.moveType == MoveType.Takeoff ? move.originOaci : move.destinationOaci;

            context.emit(airportOaci, 1);
        }
    }

    private static class MoveCountReducerFactory implements ReducerFactory<String, Integer, Integer> {
        @Override
        public Reducer<Integer, Integer> newReducer(String oaci) {
            return new MoveCountReducer();
        }

        static class MoveCountReducer extends Reducer<Integer, Integer> {
            private volatile AtomicInteger moves;

            @Override
            public void beginReduce() {
                moves = new AtomicInteger(0);
            }

            @Override
            public void reduce(Integer value) {
                moves.getAndAdd(value);
            }

            @Override
            public Integer finalizeReduce() {
                return moves.get();
            }
        }
    }

    private static class MoveCollator
            implements Collator<Map.Entry<String, Integer>, List<Triple<String, String, Integer>>> {
        private final List<Airport> airports;

        public MoveCollator(List<Airport> airports) {
            this.airports = airports;
        }

        @Override
        public List<Triple<String, String, Integer>> collate(Iterable<Map.Entry<String, Integer>> values) {
            List<Triple<String, String, Integer>> results = new ArrayList<>();

            for (Map.Entry<String, Integer> entry : values) {
                String name = airports.stream().filter(airport -> airport.oaci.equals(entry.getKey()))
                        .map(airport -> airport.name).findFirst().orElse(null);

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

    public static void main(String[] args) throws IOException, ExecutionException, InterruptedException {
        // Parse command-line arguments
        List<String> nodes = ArgumentParser.getAddresses(logger);
        String inPath = ArgumentParser.getInPath(logger);
        String outPath = ArgumentParser.getOutPath(logger);

        // Initialize HazelCast client, loading files from the specified path
        HazelcastInstance hazelClient = ClientManager.getClient(inPath, nodes);
        JobTracker jobTracker = hazelClient.getJobTracker("move-count");

        // Get references to distributed collections
        IList<Move> iMoves = hazelClient.getList(Configuration.iMoveCollectionName);
        KeyValueSource<String, Move> source = KeyValueSource.fromList(iMoves);
        IList<Airport> iAirports = hazelClient.getList(Configuration.iAirportCollectionName);

        // Create job
        Job<String, Move> job = jobTracker.newJob(source);

        // Process
        logger.info("Inicio del trabajo map/reduce");
        JobCompletableFuture<List<Triple<String, String, Integer>>> future = job.mapper(new MoveMapper())
                .reducer(new MoveCountReducerFactory()).submit(new MoveCollator(iAirports));

        // Print results
        serializeQuery(future.get(), outPath);
        logger.info("Fin del trabajo map/reduce");

        // Close Hazelcast client
        hazelClient.shutdown();
    }

    public static void serializeQuery(List<Triple<String, String, Integer>> queryResults, String outPath) {
        String[] headers = {"OACI", "Denominación", "Movimientos"};
        List<String[]> lines = new ArrayList<>();

        lines.add(headers);
        for (Triple<String, String, Integer> triple : queryResults) {
            String[] line = {triple.getLeft(), triple.getMiddle(), triple.getRight().toString()};
            lines.add(line);
        }

        Output.print(outPath + "query1.csv", lines);
    }
}
