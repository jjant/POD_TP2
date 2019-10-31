package pod.api.query2;

import pod.api.*;

import com.hazelcast.mapreduce.*;
import java.util.concurrent.atomic.AtomicInteger;

public class MoveRankingReducerFactory implements ReducerFactory<String, Integer, Integer> {
    @Override
    public Reducer<Integer, Integer> newReducer(String airline) {
        return new MoveRankingReducer();
    }

    static class MoveRankingReducer extends Reducer<Integer, Integer> {
        private volatile AtomicInteger moves;

        @Override
        public void beginReduce() {
            moves = new AtomicInteger(0);
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