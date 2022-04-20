package it.matteociccaglione.gitlogtest;
import it.matteociccaglione.gitlogtest.jira.JiraManager;
import it.matteociccaglione.gitlogtest.jira.Version;
import org.eclipse.jgit.api.errors.GitAPIException;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.util.List;

public class SimpleClassForLogTest {
    public static void main(String[] args) throws IOException, ParseException {
        System.out.println("This is the first commit");
        System.out.println("Added new print");
        System.out.println("Test");
        /*
        try {
            List<String> commits = GitLogMiningClass.getCommits("added");
            for (String com : commits) {
                System.out.println(com);
            }
        }catch(Exception e){
            e.printStackTrace();
        }
        */
        List<Version> versions = JiraManager.retrieveVersions("ZOOKEEPER");

    }
}
