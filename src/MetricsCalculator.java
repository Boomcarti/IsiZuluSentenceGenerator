import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class MetricsCalculator {

    public void calculateMetrics(String filePath) throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader(filePath));
        String line = reader.readLine(); // Skip header

        int totalWords = 0;
        Set<String> uniqueWords = new HashSet<>();
        
        while ((line = reader.readLine()) != null) {
            String[] parts = line.split(",");
            if (parts.length > 0) {
                String sentence = parts[0]; // Assuming the sentence is the first column
                String[] words = sentence.split("\\s+");

                totalWords += words.length;
                for (String word : words) {
                    uniqueWords.add(word);
                }
            }
        }
        reader.close();

        // Output metrics for this file
        System.out.println("File: " + filePath);
        System.out.println("Total Words: " + totalWords);
        System.out.println("Unique Words: " + uniqueWords.size());
    }

    public static void main(String[] args) {
        MetricsCalculator calculator = new MetricsCalculator();
        ExecutorService executor = Executors.newFixedThreadPool(4); // Create a thread pool with 4 threads

        try {
            String[] files = {"CardinalSingular2Singular.csv", "CardinalPlural2Singular.csv", "CardinalSingular2Plural.csv", "CardinalPlural2Plural.csv", "AdverbsSingularToSingular.csv", "AdverbsPluralToSingular.csv", "AdverbsSingularToPlural.csv", "AdverbsPluralToPlural.csv", "SOI.csv", "SOISentence.csv","OrdinalSingular2Plural.csv","OrdinalSingular2Singular.csv"};
            for (String file : files) {
                executor.submit(() -> {
                    try {
                        calculator.calculateMetrics(file);
                    } catch (IOException e) {
                        System.out.println("Error processing file: " + file + "; " + e.getMessage());
                    }
                });
            }
        } finally {
            executor.shutdown(); // Shutdown the executor
            try {
                if (!executor.awaitTermination(60, TimeUnit.SECONDS)) {
                    executor.shutdownNow();
                }
            } catch (InterruptedException e) {
                executor.shutdownNow();
            }
        }
    }
}
