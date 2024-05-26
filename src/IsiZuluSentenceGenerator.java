import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class IsiZuluSentenceGenerator {


    HashMap<String, String> concords;

    ArrayList<String> verbRoots; 
    Map<String, String> singularToSNC;
    Map<String, String> pluralToNC;

    String csvFilePath="";

    public IsiZuluSentenceGenerator(){
        verbRoots = new ArrayList<>();
        singularToSNC= new HashMap<>();
        pluralToNC = new HashMap<>();

        concords =new HashMap<>();
        concords.put("1", "u");
        concords.put("2", "ba");
        concords.put("1a", "u");
        concords.put("2a", "ba");
        concords.put("3a", "u");
        concords.put("3", "u");
        concords.put("4", "i");
        concords.put("5", "li");
        concords.put("6", "a");
        concords.put("7", "si");
        concords.put("8", "zi");
        concords.put("9", "i");
        concords.put("10", "zi");
        concords.put("11", "lu");
        concords.put("14", "bu");
        concords.put("15", "ku");

    }

    void readCSV(String filePath){
        String line;
        String csvSplitBy = ";";

        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            boolean firstRow = true;
      
            while ((line = br.readLine()) != null) {
              if (firstRow) {
                firstRow = false; // Skip header row
                continue;
              }
      
              String[] columns = line.split(csvSplitBy);
                String verbRoot = columns[0];
                String singular = columns[4];
                String snc = columns[5];
                String plural = columns[6];
                String pnc = columns[7];
      
      
                if(!verbRoot.equals(""))this.verbRoots.add(verbRoot);
                this.singularToSNC.put(singular, snc);
                this.pluralToNC.put(plural, pnc);
              
            }
      
          } catch (IOException e) {
            e.printStackTrace();
          }
          singularToSNC.remove("");
          pluralToNC.remove("");   
    }

void CardinalSingular2Singular() throws IOException {
        FileWriter csvWriter = new FileWriter("CardinalSingular2Singular.csv");
        csvWriter.append("sentence,first_noun,verb,second_noun,number\n");
        
        for(String verb : this.verbRoots){
            for (Map.Entry<String,String> firstNoun : this.singularToSNC.entrySet()){
                for (Map.Entry<String,String> secondNoun : this.singularToSNC.entrySet()){
                    String verbConcord = concords.get(firstNoun.getValue());
                    String completedVerb = verbConcord + verb;

                    List<String> command = new ArrayList<>();
                    command.add("java");
                    command.add("-jar");
                    // Update the path according to the location confirmed by the command line
                    command.add("src/ZuluNum2TextCMD.jar");
                    command.add("-n");
                    command.add("1");
                    command.add("-c");
                    command.add("Ca");
                    command.add("-nc");
                    command.add(secondNoun.getValue());
                    command.add("-d");
            
                    // ProcessBuilder setup
                    ProcessBuilder processBuilder = new ProcessBuilder(command);
                    processBuilder.redirectErrorStream(true);
            
                    Process process = processBuilder.start();
            
                    // Read the output from the process
                    List<String> outputLines = new ArrayList<>();
                    try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                        String line;
                        while ((line = reader.readLine()) != null) {
                            outputLines.add(line);
                        }
                    }
                    String verbalisedNumber = outputLines.get(1).split(" = ")[1];

                    String sentence = firstNoun.getKey() + " " + completedVerb + " " + secondNoun.getKey() + " " + verbalisedNumber;     
                    csvWriter.append(String.join(",", sentence, firstNoun.getKey(), completedVerb, secondNoun.getKey(), "1"));
                    csvWriter.append("\n");
                  //  System.out.println(sentence);
                }
            }
        }
        csvWriter.flush();
        csvWriter.close();
    }

    void CardinalPlural2Singular() throws IOException {
        FileWriter csvWriter = new FileWriter("CardinalPlural2Singular.csv");
        csvWriter.append("sentence,first_noun,verb,second_noun,number\n");
    
        for (String verb : this.verbRoots) {
            for (Map.Entry<String, String> firstNoun : this.pluralToNC.entrySet()) {
                for (Map.Entry<String, String> secondNoun : this.singularToSNC.entrySet()) {
                    String verbConcord = concords.get(firstNoun.getValue());
                    String completedVerb = verbConcord + verb;
    
                    List<String> command = new ArrayList<>();
                    command.add("java");
                    command.add("-jar");
                    // Update the path according to the location confirmed by the command line
                    command.add("src/ZuluNum2TextCMD.jar");
                    command.add("-n");
                    command.add("1");
                    command.add("-c");
                    command.add("Ca");
                    command.add("-nc");
                    command.add(secondNoun.getValue());
                    command.add("-d");
    
                    // ProcessBuilder setup
                    ProcessBuilder processBuilder = new ProcessBuilder(command);
                    processBuilder.redirectErrorStream(true);
    
                    Process process = processBuilder.start();
    
                    // Read the output from the process
                    List<String> outputLines = new ArrayList<>();
                    try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                        String line;
                        while ((line = reader.readLine()) != null) {
                            outputLines.add(line);
                        }
                    }
    
                    String verbalisedNumber = outputLines.get(1).split(" = ")[1];
                    String sentence = firstNoun.getKey() + " " + completedVerb + " " + secondNoun.getKey() + " " + verbalisedNumber;
    
                    csvWriter.append(String.join(",", sentence, firstNoun.getKey(), completedVerb, secondNoun.getKey(), "1"));
                    csvWriter.append("\n");
               //     System.out.println(sentence);
                }
            }
        }
    
        csvWriter.flush();
        csvWriter.close();
    }
    





    void CardinalSingular2Plural() throws IOException, InterruptedException {
        FileWriter csvWriter = new FileWriter("CardinalSingular2Plural.csv");
        csvWriter.append("sentence,first_noun,verb,second_noun,number\n");
    
        int plurallimit = 9999;
        for (int number = 2; number < plurallimit; number++) {
            for (String verb : this.verbRoots) {
                for (Map.Entry<String, String> firstNoun : this.singularToSNC.entrySet()) {
                    for (Map.Entry<String, String> secondNoun : this.pluralToNC.entrySet()) {
                        String verbConcord = concords.get(firstNoun.getValue());
                        String completedVerb = verbConcord + verb;
    
                        List<String> command = new ArrayList<>();
                        command.add("java");
                        command.add("-jar");
                        // Update the path according to the location confirmed by the command line
                        command.add("src/ZuluNum2TextCMD.jar");
                        command.add("-n");
                        command.add(String.valueOf(number));
                        command.add("-c");
                        command.add("Ca");
                        command.add("-nc");
                        command.add(secondNoun.getValue());
                        command.add("-d");
    
                        // ProcessBuilder setup
                        ProcessBuilder processBuilder = new ProcessBuilder(command);
                        processBuilder.redirectErrorStream(true);
    
                        Process process = processBuilder.start();
    
                        // Read the output from the process
                        List<String> outputLines = new ArrayList<>();
                        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                            String line;
                            while ((line = reader.readLine()) != null) {
                                outputLines.add(line);
                            }
                        }
    
                        String verbalisedNumber = outputLines.get(1).split(" = ")[1];
                        String sentence = firstNoun.getKey() + " " + completedVerb + " " + secondNoun.getKey() + " " + verbalisedNumber;
    
                        csvWriter.append(String.join(",", sentence, firstNoun.getKey(), completedVerb, secondNoun.getKey(), String.valueOf(number)));
                        csvWriter.append("\n");
                    }
                }
            }
        }
    
        csvWriter.flush();
        csvWriter.close();
    }

    void CardinalPlural2Plural() throws IOException, InterruptedException {
        FileWriter csvWriter = new FileWriter("CardinalPlural2Plural.csv");
        csvWriter.append("sentence,first_noun,verb,second_noun,number\n");
    
        int plurallimit = 9999;
        for (int number = 2; number < plurallimit; number++) {
            for (String verb : this.verbRoots) {
                for (Map.Entry<String, String> firstNoun : this.pluralToNC.entrySet()) {
                    for (Map.Entry<String, String> secondNoun : this.pluralToNC.entrySet()) {
                        String verbConcord = concords.get(firstNoun.getValue());
                        String completedVerb = verbConcord + verb;
    
                        List<String> command = new ArrayList<>();
                        command.add("java");
                        command.add("-jar");
                        // Update the path according to the location confirmed by the command line
                        command.add("src/ZuluNum2TextCMD.jar");
                        command.add("-n");
                        command.add(String.valueOf(number));
                        command.add("-c");
                        command.add("Ca");
                        command.add("-nc");
                        command.add(secondNoun.getValue());
                        command.add("-d");
    
                        // ProcessBuilder setup
                        ProcessBuilder processBuilder = new ProcessBuilder(command);
                        processBuilder.redirectErrorStream(true);
    
                        Process process = processBuilder.start();
    
                        // Read the output from the process
                        List<String> outputLines = new ArrayList<>();
                        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                            String line;
                            while ((line = reader.readLine()) != null) {
                                outputLines.add(line);
                            }
                        }
    
                        String verbalisedNumber = outputLines.get(1).split(" = ")[1];
                        String sentence = firstNoun.getKey() + " " + completedVerb + " " + secondNoun.getKey() + " " + verbalisedNumber;
    
                        csvWriter.append(String.join(",", sentence, firstNoun.getKey(), completedVerb, secondNoun.getKey(), String.valueOf(number)));
                        csvWriter.append("\n");
                    }
                }
            }
        }
    
        csvWriter.flush();
        csvWriter.close();
    }
    
    


 
    ArrayList<String> SOIStrings() throws IOException, InterruptedException{
        ArrayList<String> sentences = new ArrayList<>();

        int plurallimit =9999;
        for(int number =2; number< plurallimit; number++){

            for (Map.Entry<String,String> noun : this.pluralToNC.entrySet()){
                String sentence = noun.getKey();


                List<String> command = new ArrayList<>();
                command.add("java");
                command.add("-jar");
                // Update the path according to the location confirmed by the command line
                command.add("src/ZuluNum2TextCMD.jar");
                command.add("-n");
                command.add(String.valueOf(number));
                command.add("-c");
                command.add("SoI");
                command.add("-nc");
                command.add(noun.getValue());
                command.add("-d");
        
                // ProcessBuilder setup
                ProcessBuilder processBuilder = new ProcessBuilder(command);
                processBuilder.redirectErrorStream(true);
        
                Process process = processBuilder.start();
        
                    // Read the output from the process
                    List<String> outputLines = new ArrayList<>();
                    try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                        String line;
                        while ((line = reader.readLine()) != null) {
                            outputLines.add(line);
                        }
                    }
                    String verbalisedNumber = outputLines.get(1).split(" = ")[1];

                    sentence= sentence+ " "+ verbalisedNumber;
                    sentences.add(sentence);
                  //  System.out.println(sentence);
                

            }




        }
return sentences;




    }


    void SOI() throws IOException, InterruptedException {
        FileWriter csvWriter = new FileWriter("SOI.csv");
        csvWriter.append("sentence,number\n");
    
        int plurallimit = 9999;
        for (int number = 2; number < plurallimit; number++) {
            for (Map.Entry<String, String> noun : this.pluralToNC.entrySet()) {
                String sentence = noun.getKey();
    
                List<String> command = new ArrayList<>();
                command.add("java");
                command.add("-jar");
                // Update the path according to the location confirmed by the command line
                command.add("src/ZuluNum2TextCMD.jar");
                command.add("-n");
                command.add(String.valueOf(number));
                command.add("-c");
                command.add("SoI");
                command.add("-nc");
                command.add(noun.getValue());
                command.add("-d");
    
                // ProcessBuilder setup
                ProcessBuilder processBuilder = new ProcessBuilder(command);
                processBuilder.redirectErrorStream(true);
    
                Process process = processBuilder.start();
    
                // Read the output from the process
                List<String> outputLines = new ArrayList<>();
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        outputLines.add(line);
                    }
                }
    
                String verbalisedNumber = outputLines.get(1).split(" = ")[1];
                sentence = sentence + " " + verbalisedNumber;
    
                csvWriter.append(String.join(",", sentence, String.valueOf(number)));
                csvWriter.append("\n");
            }
        }
    
        csvWriter.flush();
        csvWriter.close();
    }
    

    void SOISentence() throws IOException, InterruptedException {
        List<String> SOIsentences = this.SOIStrings(); // Ensure SOIStrings() returns List<String>
        FileWriter csvWriter = new FileWriter("SOISentence.csv");
        csvWriter.append("sentence,verb\n");
    
        for (String verb : this.verbRoots) {
            for (Map.Entry<String, String> firstNoun : this.pluralToNC.entrySet()) {
                String verbConcord = this.concords.get(firstNoun.getValue());
                String completedVerb = verbConcord + verb;
                String sentence = firstNoun.getKey() + " " + completedVerb;
    
                for (String sentenceEnd : SOIsentences) {
                    String fsentence = sentence + " " + sentenceEnd;
                    csvWriter.append(String.join(",", fsentence, completedVerb));
                    csvWriter.append("\n");
                }
            }
        }
    
        csvWriter.flush();
        csvWriter.close();
    }
    

    void AdverbsSingularToSingular() throws IOException, InterruptedException {
        FileWriter csvWriter = new FileWriter("AdverbsSingularToSingular.csv");
        csvWriter.append("sentence,number,verbalised_number,verb\n");
        int plurallimit = 9999;
    
        for (int number = 1; number < plurallimit; number++) {
            for (String verb : this.verbRoots) {
                for (Map.Entry<String, String> firstNoun : this.singularToSNC.entrySet()) {
                    for (Map.Entry<String, String> secondNoun : this.singularToSNC.entrySet()) {
                        String verbConcord = concords.get(firstNoun.getValue());
                        String completedVerb = verbConcord + verb;
    
                        List<String> command = new ArrayList<>();
                        command.add("java");
                        command.add("-jar");
                        // Update the path according to the location confirmed by the command line
                        command.add("src/ZuluNum2TextCMD.jar");
                        command.add("-n");
                        command.add(String.valueOf(number));
                        command.add("-c");
                        command.add("A");
                        command.add("-d");
    
                        // ProcessBuilder setup
                        ProcessBuilder processBuilder = new ProcessBuilder(command);
                        processBuilder.redirectErrorStream(true);
    
                        Process process = processBuilder.start();
    
                        // Read the output from the process
                        List<String> outputLines = new ArrayList<>();
                        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                            String line;
                            while ((line = reader.readLine()) != null) {
                                outputLines.add(line);
                            }
                        }
                        String verbalisedNumber = outputLines.get(1).split(" = ")[1];
    
                        String sentence = firstNoun.getKey() + " " + completedVerb + " " + secondNoun.getKey() + " " + verbalisedNumber;
                        csvWriter.append(String.join(",", sentence, String.valueOf(number), verbalisedNumber, completedVerb));
                        csvWriter.append("\n");
                    }
                }
            }
        }
    
        csvWriter.flush();
        csvWriter.close();
    }
    



    void AdverbsSingularToPlural() throws IOException, InterruptedException {
        FileWriter csvWriter = new FileWriter("AdverbsSingularToPlural.csv");
        csvWriter.append("sentence,number,verbalised_number,verb\n");
        int plurallimit = 9999;
    
        for (int number = 1; number < plurallimit; number++) {
            for (String verb : this.verbRoots) {
                for (Map.Entry<String, String> firstNoun : this.singularToSNC.entrySet()) {
                    for (Map.Entry<String, String> secondNoun : this.pluralToNC.entrySet()) {
                        String verbConcord = concords.get(firstNoun.getValue());
                        String completedVerb = verbConcord + verb;
    
                        List<String> command = new ArrayList<>();
                        command.add("java");
                        command.add("-jar");
                        // Update the path according to the location confirmed by the command line
                        command.add("src/ZuluNum2TextCMD.jar");
                        command.add("-n");
                        command.add(String.valueOf(number));
                        command.add("-c");
                        command.add("A");
                        command.add("-d");
    
                        // ProcessBuilder setup
                        ProcessBuilder processBuilder = new ProcessBuilder(command);
                        processBuilder.redirectErrorStream(true);
    
                        Process process = processBuilder.start();
    
                        // Read the output from the process
                        List<String> outputLines = new ArrayList<>();
                        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                            String line;
                            while ((line = reader.readLine()) != null) {
                                outputLines.add(line);
                            }
                        }
                        String verbalisedNumber = outputLines.get(1).split(" = ")[1];
    
                        String sentence = firstNoun.getKey() + " " + completedVerb + " " + secondNoun.getKey() + " " + verbalisedNumber;
                        csvWriter.append(String.join(",", sentence, String.valueOf(number), verbalisedNumber, completedVerb));
                        csvWriter.append("\n");
                    }
                }
            }
        }
    
        csvWriter.flush();
        csvWriter.close();
    }
    


    void AdverbsPluralToSingular() throws IOException, InterruptedException {
        FileWriter csvWriter = new FileWriter("AdverbsPluralToSingular.csv");
        csvWriter.append("sentence,number,verbalised_number,verb\n");
        int plurallimit = 9999;
    
        for (int number = 1; number < plurallimit; number++) {
            for (String verb : this.verbRoots) {
                for (Map.Entry<String, String> firstNoun : this.pluralToNC.entrySet()) {
                    for (Map.Entry<String, String> secondNoun : this.singularToSNC.entrySet()) {
                        String verbConcord = concords.get(firstNoun.getValue());
                        String completedVerb = verbConcord + verb;
    
                        List<String> command = new ArrayList<>();
                        command.add("java");
                        command.add("-jar");
                        // Update the path according to the location confirmed by the command line
                        command.add("src/ZuluNum2TextCMD.jar");
                        command.add("-n");
                        command.add(String.valueOf(number));
                        command.add("-c");
                        command.add("A");
                        command.add("-d");
    
                        // ProcessBuilder setup
                        ProcessBuilder processBuilder = new ProcessBuilder(command);
                        processBuilder.redirectErrorStream(true);
    
                        Process process = processBuilder.start();
    
                        // Read the output from the process
                        List<String> outputLines = new ArrayList<>();
                        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                            String line;
                            while ((line = reader.readLine()) != null) {
                                outputLines.add(line);
                            }
                        }
                        String verbalisedNumber = outputLines.get(1).split(" = ")[1];
    
                        String sentence = firstNoun.getKey() + " " + completedVerb + " " + secondNoun.getKey() + " " + verbalisedNumber;
                        csvWriter.append(String.join(",", sentence, String.valueOf(number), verbalisedNumber, completedVerb));
                        csvWriter.append("\n");
                    }
                }
            }
        }
    
        csvWriter.flush();
        csvWriter.close();
    }

    void AdverbsPluralToPlural() throws IOException, InterruptedException {
        FileWriter csvWriter = new FileWriter("AdverbsPluralToPlural.csv");
        csvWriter.append("sentence,number,verbalised_number,verb\n");
        int plurallimit = 9999;
    
        for (int number = 1; number < plurallimit; number++) {
            for (String verb : this.verbRoots) {
                for (Map.Entry<String, String> firstNoun : this.pluralToNC.entrySet()) {
                    for (Map.Entry<String, String> secondNoun : this.pluralToNC.entrySet()) {
                        String verbConcord = concords.get(firstNoun.getValue());
                        String completedVerb = verbConcord + verb;
    
                        List<String> command = new ArrayList<>();
                        command.add("java");
                        command.add("-jar");
                        // Update the path according to the location confirmed by the command line
                        command.add("src/ZuluNum2TextCMD.jar");
                        command.add("-n");
                        command.add(String.valueOf(number));
                        command.add("-c");
                        command.add("A");
                        command.add("-d");
    
                        // ProcessBuilder setup
                        ProcessBuilder processBuilder = new ProcessBuilder(command);
                        processBuilder.redirectErrorStream(true);
    
                        Process process = processBuilder.start();
    
                        // Read the output from the process
                        List<String> outputLines = new ArrayList<>();
                        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                            String line;
                            while ((line = reader.readLine()) != null) {
                                outputLines.add(line);
                            }
                        }
                        String verbalisedNumber = outputLines.get(1).split(" = ")[1];
    
                        String sentence = firstNoun.getKey() + " " + completedVerb + " " + secondNoun.getKey() + " " + verbalisedNumber;
                        csvWriter.append(String.join(",", sentence, String.valueOf(number), verbalisedNumber, completedVerb));
                        csvWriter.append("\n");
                    }
                }
            }
        }
    
        csvWriter.flush();
        csvWriter.close();
    }
    




    



  public static void main(String[] args) throws IOException, InterruptedException {
    IsiZuluSentenceGenerator sentenceGenerator =  new IsiZuluSentenceGenerator();
    sentenceGenerator.readCSV("src/VerbsCSV.csv");
 sentenceGenerator.CardinalSingular2Singular();
sentenceGenerator.CardinalPlural2Singular();
 sentenceGenerator.CardinalSingular2Plural();

sentenceGenerator.CardinalPlural2Plural();

sentenceGenerator.SOI(); 
sentenceGenerator.SOISentence();
sentenceGenerator.AdverbsSingularToSingular();


 sentenceGenerator.AdverbsSingularToPlural();
sentenceGenerator.AdverbsPluralToSingular();
sentenceGenerator.AdverbsPluralToPlural();


    




 





  }

}