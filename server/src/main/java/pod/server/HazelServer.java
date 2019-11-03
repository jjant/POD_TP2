package pod.server;

import com.hazelcast.config.Config;
import com.hazelcast.config.GroupConfig;
import com.hazelcast.core.Hazelcast;

public class HazelServer {
    public static void main(String[] args) {
        final Config config = new Config();
        config.setGroupConfig(new GroupConfig("g6"));

        // Run 4 nodes
        Hazelcast.newHazelcastInstance(config);
        Hazelcast.newHazelcastInstance(config);
        Hazelcast.newHazelcastInstance(config);
        Hazelcast.newHazelcastInstance(config);
    }
}
