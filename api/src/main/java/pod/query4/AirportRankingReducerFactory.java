package pod.api.query4;

import pod.api.*;

import com.hazelcast.mapreduce.*;
import java.util.concurrent.atomic.AtomicInteger;

public class AirportRankingReducerFactory implements ReducerFactory<String, Integer, Integer> {
    public static final long serialVersionUID = 4L;

    @Override
    public Reducer<Integer, Integer> newReducer(String airline) {
        return new AirportRankingReducer();
    }

    static class AirportRankingReducer extends Reducer<Integer, Integer> {
        private volatile AtomicInteger moves;

        @Override
        public void beginReduce() {
            moves= new AtomicInteger(0);
        }

        @Override
        public void reduce(Integer value) {
            moves.getAndAdd(value);
        }

        @Override
        public Integer finalizeReduce() {
            return moves.get();
        }
    }
}