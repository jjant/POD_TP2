import com.hazelcast.client.config.*;
import com.hazelcast.client.HazelcastClient;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.*;
import com.hazelcast.mapreduce.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;

public class ClientManager {
  private static Logger logger = LoggerFactory.getLogger(Query2Client.class);
  public HazelcastInstance hazelClient = null;
  public IList<Move> iMoves;
  public IList<Airport> iAirports;
  public JobTracker jobTracker;

  public void finish() {
    hazelClient.shutdown();
  }

  public Job<String, Move> start(String job, String nodes) throws IOException {

    // Set up Hazelcast client configuration
    final ClientConfig clientConfig = new ClientConfig();
    clientConfig.setProperty("hazelcast.logging.type", "none");
    clientConfig.getNetworkConfig().setAddresses(Parse.parseNodes(nodes));
    hazelClient = HazelcastClient.newHazelcastClient(clientConfig);

    // Parse input files
    logger.info("Inicio de la lectura de los archivos");

    List<Move> moves = Parse.parseMoves();
    List<Airport> airports = Parse.parseAirports();
    iMoves = hazelClient.getList("g6-moves");
    iAirports = hazelClient.getList("g6-airports");

    iMoves.clear();
    iAirports.clear();
    iMoves.addAll(moves);
    iAirports.addAll(airports);

    logger.info("Fin de la lectura de los archivos");

    // Setup key sources
    jobTracker = hazelClient.getJobTracker(job);
    final KeyValueSource<String, Move> source = KeyValueSource.fromList(iMoves);

    return jobTracker.newJob(source);
  }

}
