package pod.api.query2;

import pod.api.*;

import com.hazelcast.mapreduce.*;

public class MoveMapper implements Mapper<String, Move, String, Integer> {
  @Override
  public void map(String s, Move move, Context<String, Integer> context) {
    if (move.flightType == FlightType.Domestic) {
      context.emit(move.airline, 1);
    }
  }
}