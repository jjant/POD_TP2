import com.hazelcast.client.config.ClientConfig;
import com.hazelcast.config.*;
import com.hazelcast.client.HazelcastClient;
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

    public static void query1() throws IOException, ExecutionException, InterruptedException {
        final Config config = new Config();
//        clientConfig.getNetworkConfig().addAddress("10.6.0.1:5701");
        final HazelcastInstance hazel = Hazelcast.newHazelcastInstance(config);

        JobTracker jobTracker = hazel.getJobTracker("word-count");

        List<Move> moves = Parse.parseMoves();

        IList<Move> imoves = hazel.getList("g3-moves");
        imoves.addAll(moves);

        final KeyValueSource<String, Move> source = KeyValueSource.fromList(imoves);

        Job<String, Move> job = jobTracker.newJob(source);


        ICompletableFuture<Map<Move, String>> future = job.mapper(new MoveMapper()).reducer(new MoveReducerFactory()).submit();

        future.get();

        System.out.println(future.get());
        System.out.println("thing finished");
    }
}
