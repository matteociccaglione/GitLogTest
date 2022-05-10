package it.matteociccaglione.gitlogtest.file;

import it.matteociccaglione.gitlogtest.jira.Classes;
import it.matteociccaglione.gitlogtest.jira.Version;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public class FileBuilder {
    private File csv;
    private FileBuilder(File csv){
        this.csv = csv;
    }

    public static FileBuilder build(String filename, List<Version> dataset) throws IOException {
        FileWriter file = new FileWriter(filename);
        StringBuilder fileContent;
        for (Version ver: dataset){

            List<Classes> classes = ver.getClasses();
            if(classes==null){
                continue;
            }
            for (Classes clas: classes){
                fileContent = new StringBuilder();
                fileContent.append(ver.getVersionNumber()).append(",");
                fileContent.append(clas.getName()).append(",").append(clas.getLocTouched().toString()).append(",").append(clas.getLocAdded().toString()).append(",").append(clas.getChurn().toString()).append(",").append(clas.getBuggy().toString());
                fileContent.append("\n");
                file.write(fileContent.toString());
            }

        }
        file.close();
        File csv = new File(filename);
        FileBuilder fb = new FileBuilder(csv);
        return fb;
    }

    public void toFlat(String filename) throws IOException {
        BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(csv)));
        String header = br.readLine();
        String[] columns = header.split(",");
        List<String> attributes = new ArrayList<>(Arrays.asList(columns).subList(2, columns.length));
        Map<String,List<String>> atValueMap = new HashMap<>();
        List<String> data = new ArrayList<>();
        String line;
        while((line = br.readLine())!=null){
            List<String> words = List.of(line.split(","));
            StringBuilder newLine = new StringBuilder();
            for (int i = 2; i < words.size(); i++){
                newLine.append(words.get(i));
                if(i!=words.size()-1){
                    newLine.append(",");
                }
            }
            data.add(newLine.toString());
        }
        for (String dat: data){
            List<String> words = List.of(dat.split(","));
            for (int i = 0; i < attributes.size(); i++){
                try{
                    Double.parseDouble(words.get(i));

                   atValueMap.put(attributes.get(i),new ArrayList<>());
                    atValueMap.get(attributes.get(i)).add("Numeric");
                }catch(NumberFormatException e){
                    if(!atValueMap.containsKey(attributes.get(i))){
                        atValueMap.put(attributes.get(i),new ArrayList<>());
                    }
                    atValueMap.get(attributes.get(i)).add(words.get(i));
                }
            }
        }
        FileWriter fw = new FileWriter(filename);
        StringBuilder fileContent = new StringBuilder();
        for (String at : attributes){
            fileContent.append("@attribute ").append(at);
            if(atValueMap.get(at).size()==1 && Objects.equals(atValueMap.get(at).get(0), "Numeric")){
                fileContent.append(" numeric\n");
            }
            else{
                fileContent.append(" {");
                for (String val : atValueMap.get(at)){
                    fileContent.append(val).append(",");
                }
                fileContent.append("}\n");
            }
        }
        fileContent.append("\n\n").append("@data");
        for (String dat : data){
            fileContent.append(dat).append("\n");
        }
        fw.write(fileContent.toString());
        fw.close();
    }

}
