import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.ICompletableFuture;
import com.hazelcast.core.IList;
import com.hazelcast.mapreduce.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

public class Query4Client {
    private static Logger logger = LoggerFactory.getLogger(Query4Client.class);

    private static class MoveMapper implements Mapper<String, Move, String, Integer> {
        public static final long serialVersionUID = 3L;
        private String originOaci;

        public MoveMapper(String originOaci) {
            this.originOaci = originOaci;
        }

        @Override
        public void map(String s, Move move, Context<String, Integer> context) {
            if (move.originOaci.equals(this.originOaci)) {
                context.emit(move.destinationOaci, 1);
            }
        }
    }

    public static class AirportRankingCollator implements Collator<Map.Entry<String, Integer>, Map<String, Long>> {
        private int N;

        public AirportRankingCollator(int N) {
            this.N = N;
        }

        @Override
        public Map<String, Long> collate(Iterable<Map.Entry<String, Integer>> values) {

            // Transform to hashmap
            Map<String, Long> totalsMap = new HashMap<>();
            for (Map.Entry<String, Integer> entry : values) {
                totalsMap.put(entry.getKey(), entry.getValue().longValue());
            }

            // Define order first by value, then lexicographically
            Comparator<Map.Entry<String, Long>> cmp = (Map.Entry<String, Long> a, Map.Entry<String, Long> b) -> {
                int valueOrder = b.getValue().compareTo(a.getValue());
                return valueOrder != 0 ? valueOrder : a.getKey().compareTo(b.getKey());
            };

            // Sort descending
            Map<String, Long> sortedMap = totalsMap.entrySet().stream().sorted(cmp).collect(
                    Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e2, LinkedHashMap::new));

            // Generate ranking map with top N
            Integer currentN = 1;
            Map<String, Long> resultMap = new LinkedHashMap<>();
            for (Map.Entry<String, Long> entry : sortedMap.entrySet()) {
                if (currentN++ > this.N)
                    break;
                resultMap.put(entry.getKey(), entry.getValue().longValue());
            }

            return resultMap;
        }
    }

    private static class AirportRankingCombinerFactory implements CombinerFactory<String, Integer, Integer> {
        @Override
        public Combiner<Integer, Integer> newCombiner(String key) {
            return new AirportRankingCombiner();
        }

        class AirportRankingCombiner extends Combiner<Integer, Integer> {
            private int sum = 0;

            @Override
            public void combine(Integer value) {
                sum++;
            }

            @Override
            public Integer finalizeChunk() {
                return sum;
            }

            @Override
            public void reset() {
                sum = 0;
            }
        }
    }

    private static class AirportRankingReducerFactory implements ReducerFactory<String, Integer, Integer> {
        public static final long serialVersionUID = 4L;

        @Override
        public Reducer<Integer, Integer> newReducer(String airline) {
            return new AirportRankingReducer();
        }

        class AirportRankingReducer extends Reducer<Integer, Integer> {
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

    public static void main(String[] args) throws IOException, ExecutionException, InterruptedException {
        // Parse command-line arguments
        List<String> nodes = ArgumentParser.getAddresses(logger);
        String inPath = ArgumentParser.getInPath(logger);
        String outPath = ArgumentParser.getOutPath(logger);
        String originOaci = ArgumentParser.getOaci(logger);
        int N = ArgumentParser.getN(logger);

        // Initialize HazelCast client, loading files from the specified path
        HazelcastInstance hazelClient = ClientManager.getClient(inPath, nodes);
        JobTracker jobTracker = hazelClient.getJobTracker("airport-ranking");

        // Get references to distributed collections
        IList<Move> iMoves = hazelClient.getList(Configuration.iMoveCollectionName);
        KeyValueSource<String, Move> source = KeyValueSource.fromList(iMoves);
        IList<Airport> iAirports = hazelClient.getList(Configuration.iAirportCollectionName);

        // Create job
        Job<String, Move> job = jobTracker.newJob(source);

        // Process
        logger.info("Inicio del trabajo map/reduce");
        ICompletableFuture<Map<String, Long>> future = job.mapper(new MoveMapper(originOaci))
                .combiner(new AirportRankingCombinerFactory()).reducer(new AirportRankingReducerFactory())
                .submit(new AirportRankingCollator(N));

        // Print results
        output(future.get(), outPath);
        logger.info("Fin del trabajo map/reduce");

        // Close Hazelcast client
        hazelClient.shutdown();
    }

    private static void output(Map<String, Long> result, String outPath) {
        String[] headers = { "OACI", "Despegues" };
        List<String[]> lines = new ArrayList<>();

        lines.add(headers);
        for (Map.Entry<String, Long> entry : result.entrySet()) {
            String[] line = { entry.getKey(), entry.getValue().toString() };
            lines.add(line);
        }

        Output.print(outPath +"query4.csv", lines);
    }

}
