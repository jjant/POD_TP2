import com.hazelcast.config.Config;
import com.hazelcast.core.Hazelcast;

import java.io.IOException;

public class HazelServer {
    public static void main(String[] args) throws IOException {
        final Config config = new Config();
        Hazelcast.newHazelcastInstance(config);
    }
}
