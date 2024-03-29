package pod.client;

import pod.api.*;

import com.opencsv.CSVParser;
import com.opencsv.CSVParserBuilder;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;

import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Parse {
    private static final int oaciIndexInCSV = 1;
    private static final int nameIndexInCSV = 4;
    private static final int provinceIndexInCSV = 21;


    public static List<Airport> parseAirports(String inPath) throws IOException {
        List<Airport> airports = new ArrayList<>();

        final CSVParser parser = new CSVParserBuilder().withSeparator(';').build();
        final CSVReader csvReader = new CSVReaderBuilder(new FileReader(inPath + "aeropuertos.csv"))
                .withCSVParser(parser)
                .withSkipLines(1)
                .build();

        String[] values;
        while ((values = csvReader.readNext()) != null) {
            String oaci = values[oaciIndexInCSV];
            String name = values[nameIndexInCSV];
            String province = values[provinceIndexInCSV];

            if (oaci.equals("")) continue;

            airports.add(Airport.of(oaci, name, province));
        }

        return airports;
    }

    public static List<Move> parseMoves(String inPath) throws IOException {
        List<Move> moves = new ArrayList<>();

        final CSVParser parser = new CSVParserBuilder().withSeparator(';').build();
        final CSVReader csvReader = new CSVReaderBuilder(new FileReader(inPath + "movimientos.csv"))
                .withCSVParser(parser)
                .withSkipLines(1)
                .build();

        String[] values;

        int flightTypeIndex = 3;
        int moveTypeIndex = 4;
        int flightClassIndex = 2;
        int originOaciIndex = 5;
        int destinationOaciIndex = 6;
        int airlineIndex = 7;

        while ((values = csvReader.readNext()) != null) {
            FlightType flightType = FlightType.parse(values[flightTypeIndex]);
            MoveType moveType = MoveType.parse(values[moveTypeIndex]);
            FlightClass flightClass = FlightClass.parse(values[flightClassIndex]);
            String originOaci = values[originOaciIndex];
            String destinationOaci = values[destinationOaciIndex];
            String airline = values[airlineIndex];

            Move move = Move.of(flightType, moveType, flightClass, originOaci, destinationOaci, airline);

            moves.add(move);
        }

        return moves;
    }
}
