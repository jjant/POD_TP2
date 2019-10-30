import com.hazelcast.config.*;
import com.hazelcast.core.Hazelcast;

import java.io.IOException;
import java.util.concurrent.ExecutionException;

public class HazelServer {
    public static void main(String[] args) throws IOException, ExecutionException, InterruptedException {
        final Config config = new Config();
        Hazelcast.newHazelcastInstance(config);
    }
}
