package pod.server;

import com.hazelcast.config.Config;
import com.hazelcast.core.Hazelcast;

public class HazelServer {
    public static void main(String[] args) {
        final Config config = new Config();
        Hazelcast.newHazelcastInstance(config);
    }
}
