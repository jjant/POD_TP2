import com.hazelcast.client.HazelcastClient;
import com.hazelcast.client.config.ClientConfig;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;

public class ClientManager {
  private static final Logger logger = LoggerFactory.getLogger(ClientManager.class);

  public static HazelcastInstance getClient(String inPath, List<String> nodes) throws IOException {
    final ClientConfig clientConfig = new ClientConfig();
    clientConfig.setProperty("hazelcast.logging.type", "none");
    clientConfig.getNetworkConfig().setAddresses(nodes);

    final HazelcastInstance hazelClient = HazelcastClient.newHazelcastClient(clientConfig);

    logger.info("Inicio de la lectura de los archivos");

    List<Move> moves = Parse.parseMoves(inPath);
    List<Airport> airports = Parse.parseAirports(inPath);
    IList<Move> iMoves = hazelClient.getList(Configuration.iMoveCollectionName);
    IList<Airport> iAirports = hazelClient.getList(Configuration.iAirportCollectionName);

    iMoves.clear();
    iAirports.clear();
    iMoves.addAll(moves);
    iAirports.addAll(airports);

    logger.info("Fin de la lectura de los archivos");

    return hazelClient;
  }
}
