import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.List;
import java.util.Map;

public class IsiZuluSentenceGenerator {
    HashMap<String, String> concords;
    Map<String, String> singularToSNC;
    Map<String, String> pluralToNC;
    Map<String, String> singular2plural;
    Map<String, String> plural2singular;
    Map<String, List<String>> singularVerbList;
    Map<String, List<String>> pluralVerbList;
    Map<String, List<String>> possibleSingularNouns;
    Map<String, List<String>> possiblePluralNouns;
    String csvFilePath="";

    public IsiZuluSentenceGenerator(){
        singularToSNC= new HashMap<>();
        pluralToNC = new HashMap<>();
        singularVerbList = new HashMap<>();
        pluralVerbList = new HashMap<>();
        possibleSingularNouns = new HashMap<>();
        possiblePluralNouns = new HashMap<>();
        singular2plural = new HashMap<>();
        plural2singular = new HashMap<>();
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
                String singular = columns[4];
                String snc = columns[5];
                String plural = columns[6];
                String pnc = columns[7];
                List<String> verbList = Arrays.asList(columns[8].split(","));
                if(verbList == null || verbList.size() == 0){
                    verbList = new ArrayList<String>();
                    verbList.add(columns[8]);
                }
                String[] possibleNouns =  columns[9].contains(("-")) ? columns[9].split("-") : new String [] {columns[9]};
                ArrayList<String> possibleSingularNounsList = new ArrayList<>();
                ArrayList<String> possiblePluralNounsList = new ArrayList<>();

                for(String possibleNounSandP : possibleNouns){
                    String singularPossibleNoun =  possibleNounSandP.replaceAll(",","").trim().split("[0-9]")[0];
                    String pluralPossibleNoun =  possibleNounSandP.replaceAll(",","").trim().split("[0-9]")[1];
                    possibleSingularNounsList.add(singularPossibleNoun);
                    possiblePluralNounsList.add(pluralPossibleNoun);
                }
                this.singular2plural.put(singular,plural);
                this.plural2singular.put(plural,singular);
                this.possibleSingularNouns.put(singular,possibleSingularNounsList);
                this.possiblePluralNouns.put(plural,possiblePluralNounsList);
                this.singularVerbList.put(singular,verbList);
                this.pluralVerbList.put(plural,verbList);
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
        System.out.println("Started: CardinalSingular2Singular");
            FileWriter csvWriter = new FileWriter("CardinalSingular2Singular.csv");
            csvWriter.append("sentence,first_noun,verb,second_noun,number\n");
                for (Map.Entry<String,String> firstNoun : this.singularToSNC.entrySet()){
                    List<String> secondNounList = this.possibleSingularNouns.get(firstNoun.getKey());
                    for (String secondNoun : secondNounList){
                        String secNounClass = this.singularToSNC.get(secondNoun);
                        if(secNounClass == null)continue;
                        List<String> verblist =  this.singularVerbList.get(firstNoun.getKey());
                        for(String verb : verblist ){
                            String verbConcord = concords.get(firstNoun.getValue());
                            String completedVerb = verbConcord + verb;                       
                            List<String> command = new ArrayList<>();
                            command.add("java");
                            command.add("-jar");

                            command.add("src/ZuluNum2TextCMD.jar");
                            command.add("-n");
                            command.add("1");
                            command.add("-c");
                            command.add("Ca");
                            command.add("-nc");
                            command.add(secNounClass);
                            command.add("-d");
                            ProcessBuilder processBuilder = new ProcessBuilder(command);
                            processBuilder.redirectErrorStream(true);

                            Process process = processBuilder.start();
                            List<String> outputLines = new ArrayList<>();
                            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                                String line;
                                while ((line = reader.readLine()) != null) {
                                    outputLines.add(line);
                                }
                            }
                            String verbalisedNumber = outputLines.get(1).split(" = ")[1];
                            if(verbalisedNumber.equals("onye")) continue;

                            String sentence = firstNoun.getKey() + " " + completedVerb + " " + secondNoun + " " + verbalisedNumber;     
                            csvWriter.append(String.join(",", sentence, firstNoun.getKey(), completedVerb, secondNoun, "1"));
                            csvWriter.append("\n");
                            csvWriter.flush();
                    }
                }
            }    
        csvWriter.close();
        System.out.println("DONE- CardinalSingular2Singular");
    }

    void CardinalPlural2Singular() throws IOException {
        System.out.println("Started: CardinalPlural2Singular");
        FileWriter csvWriter = new FileWriter("CardinalPlural2Singular.csv");
        csvWriter.append("sentence,first_noun,verb,second_noun,number\n");

            for (Map.Entry<String, String> firstNoun : this.pluralToNC.entrySet()) {
                String singular = this.plural2singular.get(firstNoun.getKey());
                List<String> secondNounList = this.possibleSingularNouns.get(singular);
                for (String secondNoun : secondNounList){
                    String secNounClass = this.singularToSNC.get(secondNoun);
                    if(secNounClass == null){                    
                        continue;}
                    List<String> verblist =  this.pluralVerbList.get(firstNoun.getKey());
                    for(String verb : verblist ){
                        String verbConcord = concords.get(firstNoun.getValue());
                        String completedVerb = verbConcord + verb;
                        
                        List<String> command = new ArrayList<>();
                        command.add("java");
                        command.add("-jar");

                        command.add("src/ZuluNum2TextCMD.jar");
                        command.add("-n");
                        command.add("1");
                        command.add("-c");
                        command.add("Ca");
                        command.add("-nc");
                        command.add(secNounClass);
                        command.add("-d");
                        
             
                        ProcessBuilder processBuilder = new ProcessBuilder(command);
                        processBuilder.redirectErrorStream(true);
                        
                        Process process = processBuilder.start();
                        
     
                        List<String> outputLines = new ArrayList<>();
                        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                            String line;
                            while ((line = reader.readLine()) != null) {
                                outputLines.add(line);
                            }
                        }
                    
                        String verbalisedNumber = outputLines.get(1).split(" = ")[1];
                        if(verbalisedNumber.equals("onye")) continue;
                        String sentence = firstNoun.getKey() + " " + completedVerb + " " + secondNoun + " " + verbalisedNumber;
                    
                        csvWriter.append(String.join(",", sentence, firstNoun.getKey(), completedVerb, secondNoun, "1"));
                        csvWriter.append("\n");
                        csvWriter.flush();
                }
            }
        }
        csvWriter.close();
        System.out.println("DONE- CardinalPlural2Singular");
    }
    





    void CardinalSingular2Plural() throws IOException, InterruptedException {
        System.out.println("Started: CardinalSingular2Plural");
        FileWriter csvWriter = new FileWriter("CardinalSingular2Plural.csv");
        csvWriter.append("sentence,first_noun,verb,second_noun,number\n");
        for (Map.Entry<String, String> firstNoun : this.singularToSNC.entrySet()) {
            String plural = this.singular2plural.get(firstNoun.getKey());
            List<String> secondNounList = this.possiblePluralNouns.get(plural);
            for (String secondNoun : secondNounList){
                String secNounClass = this.pluralToNC.get(secondNoun);
                if(secNounClass == null){continue;}
                int plurallimit = 9999;
                for (int number = 2; number < plurallimit; number++) {
                    String verbConcord = concords.get(firstNoun.getValue());
                    List<String> verblist =  this.singularVerbList.get(firstNoun.getKey());
                    for(String verb : verblist ){
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
                        command.add(secNounClass);
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
                        if(verbalisedNumber.equals("onye")) continue;
                        String sentence = firstNoun.getKey() + " " + completedVerb + " " + secondNoun + " " + verbalisedNumber;
    
                        csvWriter.append(String.join(",", sentence, firstNoun.getKey(), completedVerb, secondNoun, String.valueOf(number)));
                        csvWriter.append("\n");
                        csvWriter.flush();
                    }
                }
            }
        }
    
        
        csvWriter.close();
        System.out.println("DONE- CardinalSingular2Plural");
    }

    void CardinalPlural2Plural() throws IOException, InterruptedException {
        System.out.println("Started: CardinalPlural2Plural");
        FileWriter csvWriter = new FileWriter("CardinalPlural2Plural.csv");
        csvWriter.append("sentence,first_noun,verb,second_noun,number\n");
        for (Map.Entry<String, String> firstNoun : this.pluralToNC.entrySet()) {
            List<String> secondNounList = this.possiblePluralNouns.get(firstNoun.getKey());
            for (String secondNoun : secondNounList){
                String secNounClass = this.pluralToNC.get(secondNoun);
                if(secNounClass == null){continue;}
                String verbConcord = concords.get(firstNoun.getValue());
                List<String> verblist =  this.pluralVerbList.get(firstNoun.getKey());
                
                int plurallimit = 9999;
                for (int number = 2; number < plurallimit; number++) {
                    for(String verb : verblist ){
                        String completedVerb = verbConcord + verb;

                        List<String> command = new ArrayList<>();
                        command.add("java");
                        command.add("-jar");
                        command.add("src/ZuluNum2TextCMD.jar");
                        command.add("-n");
                        command.add(String.valueOf(number));
                        command.add("-c");
                        command.add("Ca");
                        command.add("-nc");
                        command.add(secNounClass);
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
                        if(verbalisedNumber.equals("onye")) continue;
                        String sentence = firstNoun.getKey() + " " + completedVerb + " " + secondNoun + " " + verbalisedNumber;
    
                        csvWriter.append(String.join(",", sentence, firstNoun.getKey(), completedVerb, secondNoun, String.valueOf(number)));
                        csvWriter.append("\n");
                        csvWriter.flush();
                    }
                }
            }
        }
    
        
        csvWriter.close();
        System.out.println("DONE- CardinalPlural2Plural");
    }
    
    
 
    ArrayList<String> SOIStrings() throws IOException, InterruptedException{
        ArrayList<String> sentences = new ArrayList<>();
        int plurallimit =292;
        for(int number =2; number< plurallimit; number++){
            for (Map.Entry<String,String> noun : this.pluralToNC.entrySet()){
                String sentence = noun.getKey();
                List<String> command = new ArrayList<>();
                command.add("java");
                command.add("-jar");

                command.add("src/ZuluNum2TextCMD.jar");
                command.add("-n");
                command.add(String.valueOf(number));
                command.add("-c");
                command.add("SoI");
                command.add("-nc");
                command.add(noun.getValue());
                command.add("-d");
        
      
                ProcessBuilder processBuilder = new ProcessBuilder(command);
                processBuilder.redirectErrorStream(true);
        
                Process process = processBuilder.start();

                    List<String> outputLines = new ArrayList<>();
                    try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                        String line;
                        while ((line = reader.readLine()) != null) {
                            outputLines.add(line);
                        }
                    }
                    String verbalisedNumber = outputLines.get(1).split(" = ")[1];
                    if(verbalisedNumber.equals("onye")) continue;

                    sentence= sentence+ " "+ verbalisedNumber;
                    sentences.add(sentence);
                

            }

        }
            return sentences;




    }





    void SOI() throws IOException, InterruptedException {
        System.out.println("Started: SOI");
        FileWriter csvWriter = new FileWriter("SOIStentences.csv");
        csvWriter.append("sentence,number\n");
        int plurallimit = 9999;
        for (int number = 2; number < plurallimit; number++) {
            for (Map.Entry<String, String> noun : this.pluralToNC.entrySet()) {
                String sentence = noun.getKey();
    
                List<String> command = new ArrayList<>();
                command.add("java");
                command.add("-jar");
                command.add("src/ZuluNum2TextCMD.jar");
                command.add("-n");
                command.add(String.valueOf(number));
                command.add("-c");
                command.add("SoI");
                command.add("-nc");
                command.add(noun.getValue());
                command.add("-d");
    
                ProcessBuilder processBuilder = new ProcessBuilder(command);
                processBuilder.redirectErrorStream(true);
    
                Process process = processBuilder.start();
    
                List<String> outputLines = new ArrayList<>();
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        outputLines.add(line);
                    }
                }
    
                String verbalisedNumber = outputLines.get(1).split(" = ")[1];
                if(verbalisedNumber.equals("onye")) continue;
                sentence = sentence + " " + verbalisedNumber;
    
                csvWriter.append(String.join(",", sentence, String.valueOf(number)));
                csvWriter.append("\n");
                csvWriter.flush();
            }
        }
       
        csvWriter.close();
    }

    
   

    void SOISentence() throws IOException, InterruptedException {
        System.out.println("Started: SOISentence");
        List<String> SOIsentences = this.SOIStrings(); 
        FileWriter csvWriter = new FileWriter("SOISentence.csv");
        csvWriter.append("sentence,verb\n");
    
            for (Map.Entry<String, String> firstNoun : this.pluralToNC.entrySet()) {
                String verbConcord = this.concords.get(firstNoun.getValue());

                List<String> verblist =  this.pluralVerbList.get(firstNoun.getKey());
                    for(String verb : verblist ){
                String completedVerb = verbConcord + verb;
    
                for (String sentenceEnd : SOIsentences) {

                    String pluralNoun = sentenceEnd.split(" ")[0];
                    List<String> secondNounList = this.possiblePluralNouns.get(firstNoun.getKey());
                    if(!secondNounList.contains(pluralNoun)) continue;
                   
                    String fsentence = firstNoun.getKey()+ " " +completedVerb + " " + sentenceEnd;
                    csvWriter.append(String.join(",", fsentence, completedVerb));
                    csvWriter.append("\n");
                    csvWriter.flush();
                }
            }
        }
        
        csvWriter.close();
        System.out.println("DONE- SOISentence");
    }


    void AdverbsSingularToSingular() throws IOException, InterruptedException {
        System.out.println("Started: AdverbsSingularToSingular");
        FileWriter csvWriter = new FileWriter("AdverbsSingularToSingular.csv");
        csvWriter.append("sentence,number,verbalised_number,verb\n");
        for (Map.Entry<String, String> firstNoun : this.singularToSNC.entrySet()) {
            List<String> secondNounList = this.possibleSingularNouns.get(firstNoun.getKey());
            for (String secondNoun : secondNounList){
                String secNounClass = this.singularToSNC.get(secondNoun);
                if(secNounClass == null)continue;


                List<String> verblist =  this.singularVerbList.get(firstNoun.getKey());
                int plurallimit = 9999;
    
                for (int number = 1; number < plurallimit; number++) {
                    for(String verb : verblist ){
                        String verbConcord = concords.get(firstNoun.getValue());
                        String completedVerb = verbConcord + verb;
    
                        List<String> command = new ArrayList<>();
                        command.add("java");
                        command.add("-jar");
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
                        if(verbalisedNumber.equals("onye")) continue;
    
                        String sentence = firstNoun.getKey() + " " + completedVerb + " " + secondNoun + " " + verbalisedNumber;
                        csvWriter.append(String.join(",", sentence, String.valueOf(number), verbalisedNumber, completedVerb));
                        csvWriter.append("\n");
                        csvWriter.flush();
                    }
                }
            }
        }
      
        csvWriter.close();
        System.out.println("DONE- AdverbsSingularToSingular");
    }
    



    void AdverbsSingularToPlural() throws IOException, InterruptedException {
        System.out.println("Started: AdverbsSingularToPlural");
        FileWriter csvWriter = new FileWriter("AdverbsSingularToPlural.csv");
        csvWriter.append("sentence,number,verbalised_number,verb\n");
        for (Map.Entry<String, String> firstNoun : this.singularToSNC.entrySet()) {
            String plural = this.singular2plural.get(firstNoun.getKey());
            List<String> secondNounList = this.possiblePluralNouns.get(plural);
            for (String secondNoun : secondNounList){
                String secNounClass = this.pluralToNC.get(secondNoun);
                if(secNounClass == null){
                continue;}

                String verbConcord = concords.get(firstNoun.getValue());
                List<String> verblist =  this.singularVerbList.get(firstNoun.getKey());
                int plurallimit = 9999;
    
                for (int number = 1; number < plurallimit; number++) {
                    for(String verb : verblist ){
                                String completedVerb = verbConcord + verb;
                            
                                List<String> command = new ArrayList<>();
                                command.add("java");
                                command.add("-jar");
                               
                                command.add("src/ZuluNum2TextCMD.jar");
                                command.add("-n");
                                command.add(String.valueOf(number));
                                command.add("-c");
                                command.add("A");
                                command.add("-d");
                            
                                ProcessBuilder processBuilder = new ProcessBuilder(command);
                                processBuilder.redirectErrorStream(true);
                            
                                Process process = processBuilder.start();
                          
                                List<String> outputLines = new ArrayList<>();
                                try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                                    String line;
                                    while ((line = reader.readLine()) != null) {
                                        outputLines.add(line);
                                    }
                                }
                                String verbalisedNumber = outputLines.get(1).split(" = ")[1];
                                if(verbalisedNumber.equals("onye")) continue;
                            
                                String sentence = firstNoun.getKey() + " " + completedVerb + " " + secondNoun + " " + verbalisedNumber;
                                csvWriter.append(String.join(",", sentence, String.valueOf(number), verbalisedNumber, completedVerb));
                                csvWriter.append("\n");
                                csvWriter.flush();
                            }
                        }
                    }
        }
    

        csvWriter.close();
        System.out.println("DONE- AdverbsSingularToPlural");  
    }
    


    void AdverbsPluralToSingular() throws IOException, InterruptedException {
        System.out.println("Started: AdverbsPluralToSingular");
        FileWriter csvWriter = new FileWriter("AdverbsPluralToSingular.csv");
        csvWriter.append("sentence,number,verbalised_number,verb\n");
        
                for (Map.Entry<String, String> firstNoun : this.pluralToNC.entrySet()) {

                    String singular = this.plural2singular.get(firstNoun.getKey());
                    List<String> secondNounList = this.possibleSingularNouns.get(singular);
                    for (String secondNoun : secondNounList){
                        String secNounClass = this.singularToSNC.get(secondNoun);
                        if(secNounClass == null){
                         
                            continue;}
                        List<String> verblist =  this.pluralVerbList.get(firstNoun.getKey());
                        int plurallimit = 9999;
    
                        for (int number = 1; number < plurallimit; number++) {
                                    for(String verb : verblist ){
                                        String verbConcord = concords.get(firstNoun.getValue());
                                        String completedVerb = verbConcord + verb;
                                    
                                        List<String> command = new ArrayList<>();
                                        command.add("java");
                                        command.add("-jar");
                 
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
                                        if(verbalisedNumber.equals("onye")) continue;
                                    
                                        String sentence = firstNoun.getKey() + " " + completedVerb + " " + secondNoun + " " + verbalisedNumber;
                                        csvWriter.append(String.join(",", sentence, String.valueOf(number), verbalisedNumber, completedVerb));
                                        csvWriter.append("\n");
                                        csvWriter.flush();

                                    }
                                }
                            }
        }
    

        csvWriter.close();
        System.out.println("DONE- AdverbsPluralToSingular");
    }

    void AdverbsPluralToPlural() throws IOException, InterruptedException {
        System.out.println("Started: AdverbsPluralToPlural");
        FileWriter csvWriter = new FileWriter("AdverbsPluralToPlural.csv");
        csvWriter.append("sentence,number,verbalised_number,verb\n");
                for (Map.Entry<String, String> firstNoun : this.pluralToNC.entrySet()) {
                    List<String> secondNounList = this.possiblePluralNouns.get(firstNoun.getKey());
                    for (String secondNoun : secondNounList){
                        String secNounClass = this.pluralToNC.get(secondNoun);
                        if(secNounClass == null){
                            continue;}
    
                        List<String> verblist =  this.pluralVerbList.get(firstNoun.getKey());
                        int plurallimit = 9999;
    
                        for (int number = 1; number < plurallimit; number++) {
                            for(String verb : verblist ){
                                String verbConcord = concords.get(firstNoun.getValue());
                                String completedVerb = verbConcord + verb;
                            
                                List<String> command = new ArrayList<>();
                                command.add("java");
                                command.add("-jar");
                            
                                command.add("src/ZuluNum2TextCMD.jar");
                                command.add("-n");
                                command.add(String.valueOf(number));
                                command.add("-c");
                                command.add("A");
                                command.add("-d");
                            
                            
                                ProcessBuilder processBuilder = new ProcessBuilder(command);
                                processBuilder.redirectErrorStream(true);
                            
                                Process process = processBuilder.start();
                            
                            
                                List<String> outputLines = new ArrayList<>();
                                try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                                    String line;
                                    while ((line = reader.readLine()) != null) {
                                        outputLines.add(line);
                                    }
                                }
                                String verbalisedNumber = outputLines.get(1).split(" = ")[1];
                                if(verbalisedNumber.equals("onye")) continue;
                            
                                String sentence = firstNoun.getKey() + " " + completedVerb + " " + secondNoun + " " + verbalisedNumber;
                                csvWriter.append(String.join(",", sentence, String.valueOf(number), verbalisedNumber, completedVerb));
                                csvWriter.append("\n");

                                    }
                                }
                            }
                        }
        
        csvWriter.close();
        System.out.println("DONE- AdverbsPluralToPlural");
    }




    void OrdinalSingular2Singular() throws IOException, InterruptedException {
        System.out.println("Started: OrdinalSingular2Singular");
        FileWriter csvWriter = new FileWriter("OrdinalSingular2Singular.csv");
        csvWriter.append("sentence,first_noun,verb,second_noun,number\n");
        for (Map.Entry<String, String> firstNoun : this.singularToSNC.entrySet()) {
            if(firstNoun.getKey().equals("imali")) continue;
            List<String> secondNounList = this.possibleSingularNouns.get(firstNoun.getKey());
            for (String secondNoun : secondNounList){
                String secNounClass = this.singularToSNC.get(secondNoun);
                if(secNounClass == null){continue;}
                String verbConcord = concords.get(firstNoun.getValue());
                List<String> verblist =  this.singularVerbList.get(firstNoun.getKey());               
                int plurallimit = 9999;
                for (int number = 1; number < plurallimit; number++) {
                    for(String verb : verblist ){
                        String completedVerb = verbConcord + verb;
                        List<String> command = new ArrayList<>();
                        command.add("java");
                        command.add("-jar");
                        command.add("src/ZuluNum2TextCMD.jar");
                        command.add("-n");
                        command.add(String.valueOf(number));
                        command.add("-c");
                        command.add("O");
                        command.add("-nc");
                        command.add(firstNoun.getValue());
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
                        if(verbalisedNumber.equals("onye")) continue;
                        String sentence = firstNoun.getKey() + " " +verbalisedNumber+" "+completedVerb + " " + secondNoun;
    
                        csvWriter.append(String.join(",", sentence, firstNoun.getKey(), completedVerb, secondNoun, String.valueOf(number)));
                        csvWriter.append("\n");
                        csvWriter.flush();
                    }
                }
            }
        } 
        csvWriter.close();
        System.out.println("DONE- OrdinalSingular2Singular");
    }

    void OrdinalSingular2Plural() throws IOException, InterruptedException {
        System.out.println("Started: OrdinalSingular2Plural");
        FileWriter csvWriter = new FileWriter("OrdinalSingular2Plural.csv");
        csvWriter.append("sentence,first_noun,verb,second_noun,number\n");
        for (Map.Entry<String, String> firstNoun : this.singularToSNC.entrySet()) {
            if(firstNoun.getKey().equals("imali")) continue;
            String plural = this.singular2plural.get(firstNoun.getKey());
            List<String> secondNounList = this.possiblePluralNouns.get(plural);
            for (String secondNoun : secondNounList){
                String secNounClass = this.pluralToNC.get(secondNoun);
                if(secNounClass == null){continue;}
                String verbConcord = concords.get(firstNoun.getValue());
                List<String> verblist =  this.singularVerbList.get(firstNoun.getKey());               
                int plurallimit = 9999;
                for (int number = 1; number < plurallimit; number++) {
                    for(String verb : verblist ){
                        String completedVerb = verbConcord + verb;
                        List<String> command = new ArrayList<>();
                        command.add("java");
                        command.add("-jar");
                        command.add("src/ZuluNum2TextCMD.jar");
                        command.add("-n");
                        command.add(String.valueOf(number));
                        command.add("-c");
                        command.add("O");
                        command.add("-nc");
                        command.add(firstNoun.getValue());
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
                        if(verbalisedNumber.equals("onye")) continue;
                        String sentence = firstNoun.getKey() + " " +verbalisedNumber+" "+completedVerb + " " + secondNoun;
    
                        csvWriter.append(String.join(",", sentence, firstNoun.getKey(), completedVerb, secondNoun, String.valueOf(number)));
                        csvWriter.append("\n");
                        csvWriter.flush();
                    }
                }
            }
        } 
        csvWriter.close();
        System.out.println("DONE- OrdinalSingular2Plural");
    }
    
    
    
    




    



  public static void main(String[] args) throws IOException, InterruptedException {
    IsiZuluSentenceGenerator sentenceGenerator =  new IsiZuluSentenceGenerator();
    sentenceGenerator.readCSV("src/VerbsCSV1.csv");

    ExecutorService executor = Executors.newFixedThreadPool(12); 
    executor.submit(() -> {
        try { sentenceGenerator.CardinalSingular2Singular(); } catch (Exception e) { e.printStackTrace(); }
    });
    executor.submit(() -> {
        try { sentenceGenerator.CardinalPlural2Singular(); } catch (Exception e) { e.printStackTrace(); }
    });
    executor.submit(() -> {
        try { sentenceGenerator.CardinalSingular2Plural(); } catch (Exception e) { e.printStackTrace(); }
    });
    executor.submit(() -> {
        try { sentenceGenerator.CardinalPlural2Plural(); } catch (Exception e) { e.printStackTrace(); }
    });
    executor.submit(() -> {
        try { sentenceGenerator.SOI(); } catch (Exception e) { e.printStackTrace(); }
    });
    executor.submit(() -> {
        try { sentenceGenerator.SOISentence(); } catch (Exception e) { e.printStackTrace(); }
    });
    executor.submit(() -> {
        try { sentenceGenerator.AdverbsSingularToSingular(); } catch (Exception e) { e.printStackTrace(); }
    });
    executor.submit(() -> {
        try { sentenceGenerator.AdverbsSingularToPlural(); } catch (Exception e) { e.printStackTrace(); }
    });
    executor.submit(() -> {
        try { sentenceGenerator.AdverbsPluralToSingular(); } catch (Exception e) { e.printStackTrace(); }
    });
    executor.submit(() -> {
        try { sentenceGenerator.AdverbsPluralToPlural(); } catch (Exception e) { e.printStackTrace(); }
    });
    executor.submit(() -> {
        try { sentenceGenerator.OrdinalSingular2Singular(); } catch (Exception e) { e.printStackTrace(); }
    });
    executor.submit(() -> {
        try { sentenceGenerator.OrdinalSingular2Plural(); } catch (Exception e) { e.printStackTrace(); }
    });

    
    executor.shutdown();
    try {
        executor.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
    } catch (InterruptedException e) {
        e.printStackTrace();
    }

    System.out.println("All tasks completed.");


}


    




 







}