package pod.api.query1;

import pod.api.*;

import com.hazelcast.mapreduce.*;

public class MoveMapper implements Mapper<String, Move, String, Integer> {
  @Override
  public void map(String s, Move move, Context<String, Integer> context) {
      String airportOaci = move.moveType == MoveType.Takeoff ? move.originOaci : move.destinationOaci;

      context.emit(airportOaci, 1);
  }
}