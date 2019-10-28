import java.io.IOException;
import java.util.concurrent.ExecutionException;

public class Main {
    public static void main(String[] args) throws IOException, ExecutionException, InterruptedException {
        Parse.parseAirports().forEach(System.out::println);

        System.out.println("Parsed airports");

        Query1.query1();
    }

}
