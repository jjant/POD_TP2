import com.google.common.collect.Sets;
import com.hazelcast.client.HazelcastClient;
import com.hazelcast.client.config.ClientConfig;
import com.hazelcast.core.*;
import com.hazelcast.mapreduce.*;

import java.io.IOException;
import java.io.Serializable;
import java.util.*;
import java.util.concurrent.ExecutionException;

public class Query3 implements Serializable {
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

    private static class ThousandsMapper implements Mapper<String, Integer, Integer, String> {

        @Override
        public void map(String s, Integer integer, Context<Integer, String> context) {
            int thousands = (int) (Math.floor(integer / 1000) * 1000);
            if(thousands != 0) {
                context.emit(thousands, s);
            }
        }
    }

    private static class ThousandsReducerFactory implements ReducerFactory<Integer, String, Set<Set<String>>> {

        @Override
        public Reducer<String, Set<Set<String>>> newReducer(Integer integer) {
            return new ThousandsReducer();
        }

        class ThousandsReducer extends Reducer<String, Set<Set<String>>> {
            private Set<String> all;

            @Override
            public void beginReduce() {
                all = new HashSet<>();
            }

            @Override
            public void reduce(String s) {
                all.add(s);
            }

            @Override
            public Set<Set<String>> finalizeReduce() {
                if(all.size() >= 2) {
                    return Sets.combinations(all, 2);
                    //Set<Set<String>> result = new HashSet<>();
                    //result.add(all);
                    //return result;
                } else {
                    return Collections.emptySet();
                }

            }
        }
    }

    public static void main(String[] args) throws IOException, ExecutionException, InterruptedException {
        final ClientConfig clientConfig = new ClientConfig();
        clientConfig.getNetworkConfig().addAddress("127.0.0.1:5701");
        final HazelcastInstance hazelClient = HazelcastClient.newHazelcastClient(clientConfig);

        JobTracker jobTracker = hazelClient.getJobTracker("move-count");
        IList<Move> iMoves = hazelClient.getList("g6-moves");

        final KeyValueSource<String, Move> source = KeyValueSource.fromList(iMoves);

        Job<String, Move> job = jobTracker.newJob(source);

        ICompletableFuture<Map<String, Integer>> future = job.mapper(new MoveMapper()).reducer(new MoveCountReducerFactory()).submit();

        Map<String, Integer> map = future.get();
        IMap<String, Integer> imap = hazelClient.getMap("imap");
        imap.putAll(map);

        Job<String, Integer> job2 = jobTracker.newJob(KeyValueSource.fromMap(imap));

        JobCompletableFuture<Map<Integer, Set<Set<String>>>> future2 = job2.mapper(new ThousandsMapper()).reducer(new ThousandsReducerFactory()).submit();

        System.out.println(future2.get());
        System.out.println("thing finished");
    }
}
