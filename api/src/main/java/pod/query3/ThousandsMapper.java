package pod.api.query3;

import pod.api.*;

import com.hazelcast.mapreduce.*;

/**
 * Groups Airports by number of (thousand) moves
 */
public class ThousandsMapper implements Mapper<String, Integer, Integer, String> {
  @Override
  public void map(String s, Integer moveCount, Context<Integer, String> context) {
    int thousands = (int) (Math.floor(moveCount.doubleValue() / 1000L) * 1000L);

    if (thousands != 0) {
      context.emit(thousands, s);
    }
  }
}