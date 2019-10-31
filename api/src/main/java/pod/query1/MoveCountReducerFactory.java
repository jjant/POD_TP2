package pod.api.query1;

import pod.api.*;

import com.hazelcast.mapreduce.*;
import java.util.concurrent.atomic.AtomicInteger;

public class MoveCountReducerFactory implements ReducerFactory<String, Integer, Integer> {
  @Override
  public Reducer<Integer, Integer> newReducer(String oaci) {
      return new MoveCountReducer();
  }

  static class MoveCountReducer extends Reducer<Integer, Integer> {
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