package it.matteociccaglione.gitlogtest;
import org.eclipse.jgit.api.errors.GitAPIException;

import java.io.IOException;
import java.util.List;

public class SimpleClassForLogTest {
    public static void main(String[] args){
        System.out.println("This is the first commit");
        System.out.println("Added new print");
        System.out.println("Test");
        try {
            List<String> commits = GitLogMiningClass.getCommits("added");
            for (String com : commits) {
                System.out.println(com);
            }
        }catch(Exception e){
            e.printStackTrace();
        }
    }
}
