import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class ExecuteJar {
    public static void main(String[] args) {
        // Command and arguments
        List<String> command = new ArrayList<>();
        command.add("java");
        command.add("-jar");
        // Update the path according to the location confirmed by the command line
        command.add("src/ZuluNum2TextCMD.jar");
        command.add("-n");
        command.add("459");
        command.add("-c");
        command.add("A");
        command.add("-d");

        // ProcessBuilder setup
        ProcessBuilder processBuilder = new ProcessBuilder(command);
        processBuilder.redirectErrorStream(true); // Redirect errors to standard output

        try {
            // Start the process
            Process process = processBuilder.start();

            // Read the output from the process
            List<String> outputLines = new ArrayList<>();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    outputLines.add(line);
                }
            }

            // Wait for the process to finish
            int exitCode = process.waitFor();
            if (exitCode == 0) {
                System.out.println("Process completed successfully.");
                String verbalised = outputLines.get(1).split(" = ")[1];
                System.out.println(verbalised);
            } else {
                System.out.println("Process failed with exit code " + exitCode);
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }
}
