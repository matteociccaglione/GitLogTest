package it.matteociccaglione.gitlogtest.file;

import it.matteociccaglione.gitlogtest.jira.Classes;
import it.matteociccaglione.gitlogtest.jira.Version;
import it.matteociccaglione.gitlogtest.weka.WekaResults;

import java.io.*;
import java.util.*;

public class FileBuilder {
    private File csv;
    private FileBuilder(File csv){
        this.csv = csv;
    }

    public static FileBuilder build(String filename, List<Version> dataset, String header) throws IOException {
        File csv = null;
        try (FileWriter file = new FileWriter(filename)) {

            StringBuilder fileContent = new StringBuilder();
            fileContent.append(header).append("\n");
            file.write(fileContent.toString());
            for (Version ver : dataset) {

                List<Classes> classes = ver.getClasses();
                if (classes == null) {
                    continue;
                }
                for (Classes clas : classes) {
                    fileContent = new StringBuilder();

                    fileContent.append(ver.getVersionNumber()).append(",");
                    fileContent.append(clas.getName()).append(",").append(clas.getLocTouched().toString()).append(",").append(clas.getLocAdded().toString()).append(",").append(clas.getChurn().toString()).append(",").append(clas.getnAuth().toString()).append(",").append(clas.getMaxLocAdded().toString()).append(",").append(clas.getMaxChurn().toString()).append(",").append(clas.getAvgLocAdded().toString()).append(",").append(clas.getAvgChurn()).append(",").append(clas.getnFix()).append(",").append(clas.getNr()).append(",").append(clas.getBuggy().toString());
                    fileContent.append("\n");
                    file.write(fileContent.toString());
                }

            }
            csv = new File(filename);

        }
        return new FileBuilder(csv);
    }
    private void handleData(String at, StringBuilder fileContent, Map<String, List<String>> atValueMap){
        if (at.equals("Buggy")) {
            fileContent.append("true").append(",").append("false");
        } else {
            for (int i = 0; i < atValueMap.get(at).size(); i++) {
                String val = atValueMap.get(at).get(i);
                fileContent.append(val);
                if (i != atValueMap.get(at).size() - 1) {
                    fileContent.append(",");
                }
            }
        }
    }
    private void convertToFlat2(List<String> attributes, Map<String, List<String>> atValueMap,StringBuilder fileContent){
        for (String at : attributes) {
            fileContent.append("@attribute ").append(at);
            if (atValueMap.get(at).size() == 1 && Objects.equals(atValueMap.get(at).get(0), "Numeric")) {
                fileContent.append(" numeric\n");
            } else {
                fileContent.append(" {");
                handleData(at,fileContent,atValueMap);
                fileContent.append("}\n");
            }
        }
    }
    private void convertToFlat(String filename, String projectName, List<String> attributes, Map<String, List<String>> atValueMap,List<String> data) throws IOException {
        try(FileWriter fw = new FileWriter(filename)) {
            StringBuilder fileContent = new StringBuilder();
            fileContent.append("@relation ").append(projectName.toLowerCase()).append("\n");
            convertToFlat2(attributes,atValueMap,fileContent);
            fileContent.append("\n\n").append("@data\n");
            for (String dat : data) {
                fileContent.append(dat).append("\n");
            }
            fw.write(fileContent.toString());
        }
    }
    private void fromData(List<String> data, List<String> attributes, Map<String, List<String>> atValueMap){
        for (String dat : data) {
            List<String> words = List.of(dat.split(","));
            for (int i = 0; i < attributes.size(); i++) {
                try {
                    Double.parseDouble(words.get(i));

                    atValueMap.put(attributes.get(i), new ArrayList<>());
                    atValueMap.get(attributes.get(i)).add("Numeric");
                } catch (NumberFormatException e) {
                    if (!atValueMap.containsKey(attributes.get(i))) {
                        atValueMap.put(attributes.get(i), new ArrayList<>());
                    }
                    if (!atValueMap.get(attributes.get(i)).contains(words.get(i))) {
                        atValueMap.get(attributes.get(i)).add(words.get(i));
                    }
                }
            }
        }
    }
    public void toFlat(String filename) throws IOException {
        try (BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(csv)))) {
            String header = br.readLine();
            String[] columns = header.split(",");
            String[] path = filename.split("/");
            String projectName = path[path.length - 1].split("\\.")[0];
            List<String> attributes = new ArrayList<>(Arrays.asList(columns).subList(2, columns.length));
            Map<String, List<String>> atValueMap = new HashMap<>();
            List<String> data = new ArrayList<>();
            String line;
            while ((line = br.readLine()) != null) {
                List<String> words = List.of(line.split(","));
                StringBuilder newLine = new StringBuilder();
                for (int i = 2; i < words.size(); i++) {
                    newLine.append(words.get(i));
                    if (i != words.size() - 1) {
                        newLine.append(",");
                    }
                }
                data.add(newLine.toString());
            }
            fromData(data,attributes,atValueMap);
            this.convertToFlat(filename,projectName,attributes,atValueMap,data);
        }
    }

    public static void buildWekaCsv(List<List<WekaResults>> results, String header, String datasetName, String fileName) throws IOException {
        StringBuilder fileContent = new StringBuilder(header).append("\n");
        for (int i = 0; i < results.get(0).size(); i++){
            for (List<WekaResults> wekaResults : results) {
                WekaResults result = wekaResults.get(i);
                fileContent.append(datasetName).append(",").append(result.getnReleases()).append(",").append(result.getPerTraining()).append(",").append(String.valueOf(result.getPerDefTraining())).append(",").append(result.getPerDefTesting()).append(",").append(result.getClassifier().toString()).append(",").append(result.getBalancing()).append(",").append(result.getFeatureSelection()).append(",").append(result.getCostSensitiveType().toString()).append(",").append(result.getTp()).append(",").append(result.getFp()).append(",").append(result.getTn()).append(",").append(result.getFn()).append(",").append(result.getPrecision()).append(",").append(result.getRecall()).append(",").append(result.getAuc()).append(",").append(result.getKappa()).append("\n");
            }
        }
        try(FileWriter fileWriter = new FileWriter(fileName)) {
            fileWriter.write(fileContent.toString());
        }
    }

    public static Configuration readConfiguration(File file) throws IOException, IllegalHeaderException {
        try (BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(new FileInputStream(file)));){
            String header = bufferedReader.readLine();
            if(!header.equalsIgnoreCase("projectName1,projectName2,projectPath1,projectPath2,projectOutputDirectory")){
                throw new IllegalHeaderException("Illegal header");
            }
            String line = bufferedReader.readLine();

            String[] parts = line.split(",");
            return new Configuration(parts[0], parts[1], parts[2], parts[3],parts[4]);
        }
    }
}
