package pod.client;

import pod.api.*;

import com.opencsv.CSVWriter;

import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.List;

public class Output {
    public static void print(String pathname, List<String[]> lines) {
        try (Writer writer = new FileWriter(pathname)) {
            CSVWriter csvWriter = new CSVWriter(writer, ';', CSVWriter.NO_QUOTE_CHARACTER, CSVWriter.DEFAULT_ESCAPE_CHARACTER,
                    CSVWriter.DEFAULT_LINE_END);

            for (String[] line : lines)
                csvWriter.writeNext(line);

            csvWriter.close();
        } catch (IOException e) {
            System.exit(1);
        }
    }
}
