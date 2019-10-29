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

// TODO: Add combiner
public class Query4Client {
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
                if (currentN++ > this.N) break;
                resultMap.put(entry.getKey(), entry.getValue().longValue());
            }

            return resultMap;
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
        final ClientConfig clientConfig = new ClientConfig();
        clientConfig.getNetworkConfig().addAddress("127.0.0.1:5701");
        final HazelcastInstance hazelClient = HazelcastClient.newHazelcastClient(clientConfig);
        int N = 5; // TODO: Receive parameter
        String originOaci = "SAEZ"; // TODO: Receive parameter

        JobTracker jobTracker = hazelClient.getJobTracker("airport-ranking");
        IList<Move> iMoves = hazelClient.getList("g6-moves");

        final KeyValueSource<String, Move> source = KeyValueSource.fromList(iMoves);

        Job<String, Move> job = jobTracker.newJob(source);

        ICompletableFuture<Map<String, Long>> future = job.mapper(new MoveMapper(originOaci))
                .reducer(new AirportRankingReducerFactory()).submit(new AirportRankingCollator(N));

        System.out.println(future.get());
        System.out.println("thing finished");
    }
}
