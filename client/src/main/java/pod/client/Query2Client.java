package pod.client;

import pod.api.*;
import pod.api.query2.*;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.ICompletableFuture;
import com.hazelcast.core.IList;
import com.hazelcast.mapreduce.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class Query2Client {
    private static final Logger logger = LoggerFactory.getLogger(Query2Client.class);

    public static class MoveRankingCollator implements Collator<Map.Entry<String, Integer>, Map<String, Double>> {
        private final int N;

        public MoveRankingCollator(int N) {
            this.N = N;
        }

        @Override
        public Map<String, Double> collate(Iterable<Map.Entry<String, Integer>> values) {
            // Calculate total amount of movements
            Long total = 0L;
            for (Map.Entry<String, Integer> entry : values) {
                total += entry.getValue();
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
            int currentN = 1;
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

    public static void main(String[] args) throws IOException, ExecutionException, InterruptedException {
        // Parse command-line arguments
        List<String> nodes = ArgumentParser.getAddresses(logger);
        String inPath = ArgumentParser.getInPath(logger);
        String outPath = ArgumentParser.getOutPath(logger);
        int N = ArgumentParser.getN(logger);

        // Initialize HazelCast client, loading files from the specified path
        HazelcastInstance hazelClient = ClientManager.getClient(inPath, nodes);
        JobTracker jobTracker = hazelClient.getJobTracker("move-ranking");

        // Get references to distributed collections
        IList<Move> iMoves = hazelClient.getList(Configuration.iMoveCollectionName);
        KeyValueSource<String, Move> source = KeyValueSource.fromList(iMoves);
        IList<Airport> iAirports = hazelClient.getList(Configuration.iAirportCollectionName);

        // Create job
        Job<String, Move> job = jobTracker.newJob(source);

        // Process
        logger.info("Inicio del trabajo map/reduce");
        ICompletableFuture<Map<String, Double>> future = job.mapper(new MoveMapper())
                .combiner(new MoveRankingCombinerFactory()).reducer(new MoveRankingReducerFactory())
                .submit(new MoveRankingCollator(N));

        // Print results
        output(future.get(), outPath);
        logger.info("Fin del trabajo map/reduce");

        // Close Hazelcast client
        hazelClient.shutdown();
    }

    private static void output(Map<String, Double> result, String outPath) {
        String[] headers = {"Aerolínea", "Porcentaje"};
        List<String[]> lines = new ArrayList<>();

        lines.add(headers);
        for (Map.Entry<String, Double> entry : result.entrySet()) {
            String[] line = {entry.getKey(),
                    String.format(java.util.Locale.US, "%.2f", entry.getValue()) + "%"};
            lines.add(line);
        }

        Output.print(outPath + "query2.csv", lines);
    }
}
