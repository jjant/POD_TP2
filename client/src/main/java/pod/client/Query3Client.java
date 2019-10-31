package pod.client;

import pod.api.*;
import pod.api.query3.*;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.ICompletableFuture;
import com.hazelcast.core.IList;
import com.hazelcast.core.IMap;
import com.hazelcast.mapreduce.*;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;

public class Query3Client {
    private static final Logger logger = LoggerFactory.getLogger(Query3Client.class);

    /**
     * Sort groups by move count and filter out groups with no airport-pairs
     */
    private static class GroupsCollator implements
            Collator<Map.Entry<Integer, List<Pair<String, String>>>, List<Pair<Integer, List<Pair<String, String>>>>> {
        @Override
        public List<Pair<Integer, List<Pair<String, String>>>> collate(
                Iterable<Map.Entry<Integer, List<Pair<String, String>>>> values) {
            List<Pair<Integer, List<Pair<String, String>>>> results = new ArrayList<>();

            for (Map.Entry<Integer, List<Pair<String, String>>> entry : values) {
                List<Pair<String, String>> airportPairs = entry.getValue();
                if (airportPairs.isEmpty()) {
                    continue;
                }

                // Sorts airportPairs alphabetically, first on the left airport, then on the right airport
                airportPairs.sort(Comparator.comparing((Function<Pair<String, String>, String>) Pair::getLeft).thenComparing(Pair::getRight));

                Pair<Integer, List<Pair<String, String>>> group = new ImmutablePair<>(entry.getKey(), airportPairs);

                results.add(group);
            }

            results.sort((pair1, pair2) -> pair2.getLeft() - pair1.getLeft());

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
        JobTracker jobTracker = hazelClient.getJobTracker("move-pairs");

        // Get references to distributed collections
        IList<Move> iMoves = hazelClient.getList(Configuration.iMoveCollectionName);
        KeyValueSource<String, Move> source = KeyValueSource.fromList(iMoves);
        IList<Airport> iAirports = hazelClient.getList(Configuration.iAirportCollectionName);

        // Create job
        Job<String, Move> job = jobTracker.newJob(source);

        // Start map/reduce
        logger.info("Inicio del trabajo map/reduce");
        ICompletableFuture<Map<String, Integer>> future = job.mapper(new MoveMapper())
                .reducer(new MoveCountReducerFactory()).submit();

        // Load results in a temporary map in the server
        Map<String, Integer> map = future.get();
        IMap<String, Integer> iMap = hazelClient.getMap("imap:oaci-moves");
        iMap.clear();
        iMap.putAll(map);

        // Start second map/reduce
        Job<String, Integer> job2 = jobTracker.newJob(KeyValueSource.fromMap(iMap));
        JobCompletableFuture<List<Pair<Integer, List<Pair<String, String>>>>> future2 = job2
                .mapper(new ThousandsMapper()).reducer(new ThousandsReducerFactory()).submit(new GroupsCollator());

        // Output CSV
        output(future2.get(), outPath);
        logger.info("Fin del trabajo map/reduce");

        // Close Hazelcast client
        hazelClient.shutdown();
    }

    private static void output(List<Pair<Integer, List<Pair<String, String>>>> queryResults, String outPath) {
        String[] headers = {"Grupo", "Aeropuerto A", "Aeropuerto B"};
        List<String[]> lines = new ArrayList<>();

        lines.add(headers);
        for (Pair<Integer, List<Pair<String, String>>> group : queryResults) {
            Integer thousands = group.getLeft();
            List<Pair<String, String>> pairs = group.getRight();

            for (Pair<String, String> pair : pairs) {
                String[] line = {thousands.toString(), pair.getLeft(), pair.getRight()};
                lines.add(line);
            }
        }

        Output.print(outPath + "query3.csv", lines);
    }

}
