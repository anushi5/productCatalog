package helper;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class CsvHelper {

    public CsvHelper() {
    }

    private boolean isFileExists(Path path) {
        return Files.exists(path) && Files.isRegularFile(path);
    }

    public Optional<String> getCsvFilePath(String filePath) throws IOException {
        Path path = Paths.get(filePath);
        if (isFileExists(path)) {
            String mimeType = Files.probeContentType(path);
            if (mimeType != null && mimeType.equals("text/csv")) {
                System.out.println("File is already a CSV file");
                return Optional.of(filePath);
            } else if (mimeType != null && mimeType.equals("text/tab-separated-values")) {
                System.out.println("File is a TSV file. Converting to CSV...");
                List<String> lines = Files.readAllLines(path);
                List<String> csvLines = lines.stream()
                        .map(line -> Arrays.stream(line.split("\t"))
                                .map(field -> field.contains(",") ? getField(field) : field)
                                .collect(Collectors.joining(",")))
                        .collect(Collectors.toList());
                Path csvPath = Paths.get(filePath.replace(".tsv", ".csv"));
                Files.write(csvPath, csvLines);
                System.out.println("Conversion complete. CSV file saved at: " + csvPath.toString());
                return Optional.of(csvPath.toString());
            } else {
                System.out.println("File is neither csv nor tsv");
            }
        } else {
            System.out.println("File doesn't exists or File is not regular type");
        }
        return Optional.empty();
    }

    private String getField(String field){
        String[] strs = field.split(",");
        StringBuilder ans = new StringBuilder();
        for(String s : strs){
            if(ans.toString() == "")
                ans = new StringBuilder(s);
            else
                ans.append(" ").append(s);
        }
        return ans.toString();
    }

}
