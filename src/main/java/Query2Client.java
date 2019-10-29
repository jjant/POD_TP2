import com.hazelcast.client.HazelcastClient;
import com.hazelcast.client.config.ClientConfig;
import com.hazelcast.config.*;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.ICompletableFuture;
import com.hazelcast.core.IList;
import com.hazelcast.mapreduce.*;

import java.io.IOException;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

public class Query2Client {
    private static class MoveMapper implements Mapper<String, Move, String, Integer> {
        public static final long serialVersionUID = 1L;

        @Override
        public void map(String s, Move move, Context<String, Integer> context) {
            if (move.flightType == FlightType.Domestic) {
                context.emit(move.airline, 1);
            }
        }
    }
    // { Aerolíneas Argentinas: [1, 1, 1, 1, 1], Flybondi: [1] }

    public static class MoveRankingCollator implements Collator<Map.Entry<String, Integer>, Map<String, Double>> {
        @Override
        public Map<String, Double> collate(Iterable<Map.Entry<String, Integer>> values) {
            int N = 5;

            // Calculate total amount of movements
            Long total = 0L;
            for (Map.Entry<String, Integer> entry : values) {
                total += entry.getValue().intValue();
            }

            // Calculate percentages
            Map<String, Double> percentagesMap = new HashMap<>();
            for (Map.Entry<String, Integer> entry : values) {
                Double percentage = entry.getValue().doubleValue() / new Double(total) * 100;
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
            Double accumulatedOtherPercentage = 0.0;
            for (Map.Entry<String, Double> entry : sortedMap.entrySet()) {
                if (currentN++ > N) {
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

    public static void main(String[] args) throws IOException, ExecutionException, InterruptedException {
        final ClientConfig clientConfig = new ClientConfig();
        clientConfig.getNetworkConfig().addAddress("127.0.0.1:5701");
        final HazelcastInstance hazelClient = HazelcastClient.newHazelcastClient(clientConfig);

        JobTracker jobTracker = hazelClient.getJobTracker("move-ranking");
        IList<Move> iMoves = hazelClient.getList("g6-moves");

        final KeyValueSource<String, Move> source = KeyValueSource.fromList(iMoves);

        Job<String, Move> job = jobTracker.newJob(source);

        ICompletableFuture<Map<String, Double>> future = job.mapper(new MoveMapper())
                .reducer(new MoveRankingReducerFactory()).submit(new MoveRankingCollator());

        System.out.println(future.get());
        System.out.println("thing finished");
    }
}
