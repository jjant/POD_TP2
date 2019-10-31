package pod.api.query4;

import pod.api.*;

import com.hazelcast.mapreduce.*;

public class MoveMapper implements Mapper<String, Move, String, Integer> {
  public static final long serialVersionUID = 3L;
  private final String originOaci;

  public MoveMapper(String originOaci) {
      this.originOaci = originOaci;
  }

  @Override
  public void map(String s, Move move, Context<String, Integer> context) {
      if (move.originOaci.equals(this.originOaci)) {
          context.emit(move.destinationOaci, 1);
      }
  }
}