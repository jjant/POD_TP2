import com.hazelcast.client.HazelcastClient;
import com.hazelcast.client.config.ClientConfig;
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

public class Query1 {
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

    public static void main(String[] args) throws IOException, ExecutionException, InterruptedException {
        final ClientConfig clientConfig = new ClientConfig();
        clientConfig.getNetworkConfig().addAddress("127.0.0.1:5701");
        final HazelcastInstance hazelClient = HazelcastClient.newHazelcastClient(clientConfig);

        JobTracker jobTracker = hazelClient.getJobTracker("move-count");
        IList<Move> iMoves = hazelClient.getList("g6-moves");

        final KeyValueSource<String, Move> source = KeyValueSource.fromList(iMoves);

        Job<String, Move> job = jobTracker.newJob(source);

        ICompletableFuture<Map<String, Integer>> future = job.mapper(new MoveMapper()).reducer(new MoveCountReducerFactory()).submit();

        System.out.println(future.get());
        System.out.println("thing finished");
    }
}
