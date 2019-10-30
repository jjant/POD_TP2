import com.hazelcast.client.HazelcastClient;
import com.hazelcast.client.config.ClientConfig;
import com.hazelcast.config.*;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.ICompletableFuture;
import com.hazelcast.core.IList;
import com.hazelcast.mapreduce.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

public class Query2Client {
    private static Logger logger = LoggerFactory.getLogger(Query2Client.class);

    private static class MoveMapper implements Mapper<String, Move, String, Integer> {
        public static final long serialVersionUID = 1L;

        @Override
        public void map(String s, Move move, Context<String, Integer> context) {
            if (move.flightType == FlightType.Domestic) {
                context.emit(move.airline.equals("N/A") ? "" + move.hashCode() : move.airline, 1);
            }
        }
    }

    public static class MoveRankingCollator implements Collator<Map.Entry<String, Integer>, Map<String, Double>> {
        private int N;

        public MoveRankingCollator(int N) {
            this.N = N;
        }

        @Override
        public Map<String, Double> collate(Iterable<Map.Entry<String, Integer>> values) {
            // Calculate total amount of movements
            Long total = 0L;
            for (Map.Entry<String, Integer> entry : values) {
                total += entry.getValue().intValue();
            }

            // Calculate percentages
            Map<String, Double> percentagesMap = new HashMap<>();
            Double percentageNA = 0.0;
            for (Map.Entry<String, Integer> entry : values) {
                Double percentage = entry.getValue().doubleValue() / new Double(total) * 100;
                if (entry.getKey().equals("N/A")) {
                    percentageNA = percentage;
                    continue;
                }
                percentagesMap.put(entry.getKey(), percentage);
            }

            // Define order first by value, then lexicographically
            Comparator<Map.Entry<String, Double>> cmp = (Map.Entry<String, Double> a, Map.Entry<String, Double> b) -> {
                int valueOrder = b.getValue().compareTo(a.getValue());
                return valueOrder != 0 ? valueOrder : a.getKey().compareTo(b.getKey());
            };

            // Sort descending
            Map<String, Double> sortedMap = percentagesMap.entrySet().stream().sorted(cmp).collect(
                    Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e2, LinkedHashMap::new));

            // Generate ranking map with top N + Others
            Integer currentN = 1;
            Map<String, Double> resultMap = new LinkedHashMap<>();
            Double accumulatedOtherPercentage = percentageNA;
            for (Map.Entry<String, Double> entry : sortedMap.entrySet()) {
                if (currentN++ > this.N) {
                    accumulatedOtherPercentage += entry.getValue();
                    continue;
                }
                Double truncatedPercentage = Math.floor(entry.getValue() * 100) / 100;
                resultMap.put(entry.getKey(), truncatedPercentage);
            }
            Double truncatedOtherPercentage = Math.floor(accumulatedOtherPercentage * 100) / 100;
            resultMap.put("Otros", truncatedOtherPercentage);

            return resultMap;
        }
    }

    private static class MoveRankingCombinerFactory implements CombinerFactory<String, Integer, Integer> {
        @Override
        public Combiner<Integer, Integer> newCombiner(String key) {
            return new MoveRankingCombiner();
        }

        class MoveRankingCombiner extends Combiner<Integer, Integer> {
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

    private static class MoveRankingReducerFactory implements ReducerFactory<String, Integer, Integer> {
        public static final long serialVersionUID = 2L;

        @Override
        public Reducer<Integer, Integer> newReducer(String airline) {
            return new MoveRankingReducer();
        }

        class MoveRankingReducer extends Reducer<Integer, Integer> {
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

    private static void output(Map<String, Double> result) {
        String[] headers = { "Aerolínea", "Porcentaje" };
        List<String[]> lines = new ArrayList<>();

        lines.add(headers);
        for (Map.Entry<String, Double> entry : result.entrySet()) {
            String[] line = { entry.getKey(),
                    String.format(java.util.Locale.US, "%.2f", entry.getValue().doubleValue()) + "%" };
            lines.add(line);
        }

        Output.print("query2.csv", lines);
    }

    public static void main(String[] args) throws IOException, ExecutionException, InterruptedException {
        ClientManager client = new ClientManager();

        // Receive parameters (TODO)
        int N = 5;
        String nodes = "127.0.0.1:5701";

        // Create job
        Job<String, Move> job = client.start("move-ranking", nodes);

        // Process
        logger.info("Inicio del trabajo map/reduce");
        ICompletableFuture<Map<String, Double>> future = job.mapper(new MoveMapper())
                .combiner(new MoveRankingCombinerFactory()).reducer(new MoveRankingReducerFactory())
                .submit(new MoveRankingCollator(N));

        // Print results
        output(future.get());
        logger.info("Fin del trabajo map/reduce");

        // Close Hazelcast client
        client.finish();
    }
}
