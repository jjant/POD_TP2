package pod.api.query4;

import pod.api.*;

import com.hazelcast.mapreduce.*;
import java.util.concurrent.atomic.AtomicInteger;

public class AirportRankingCombinerFactory implements CombinerFactory<String, Integer, Integer> {
    @Override
    public Combiner<Integer, Integer> newCombiner(String key) {
        return new AirportRankingCombiner();
    }

    static class AirportRankingCombiner extends Combiner<Integer, Integer> {
        private int sum = 0;

        @Override
        public void combine(Integer value) {
            sum++;
        }

        @Override
        public Integer finalizeChunk() {
            return sum;
        }

        @Override
        public void reset() {
            sum = 0;
        }
    }
}