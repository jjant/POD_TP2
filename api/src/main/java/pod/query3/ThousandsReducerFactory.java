package pod.api.query3;

import pod.api.*;

import com.hazelcast.mapreduce.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

public class ThousandsReducerFactory implements ReducerFactory<Integer, String, List<Pair<String, String>>> {

    @Override
    public Reducer<String, List<Pair<String, String>>> newReducer(Integer integer) {
        return new ThousandsReducer();
    }

    static class ThousandsReducer extends Reducer<String, List<Pair<String, String>>> {
        private List<String> airportsToPair;

        @Override
        public void beginReduce() {
            airportsToPair = new ArrayList<>();
        }

        @Override
        public void reduce(String s) {
            airportsToPair.add(s);
        }

        @Override
        public List<Pair<String, String>> finalizeReduce() {
            List<Pair<String, String>> result = new ArrayList<>();

            airportsToPair.sort(String::compareTo);

            if (airportsToPair.size() >= 2) {
                for (int i = 0; i < airportsToPair.size(); i++) {
                    for (int j = i + 1; j < airportsToPair.size(); j++) {
                        String oaci1 = airportsToPair.get(i);
                        String oaci2 = airportsToPair.get(j);

                        result.add(new ImmutablePair<>(oaci1, oaci2));
                    }
                }
            }

            return result;
        }
    }
}