package it.matteociccaglione.gitlogtest;
import it.matteociccaglione.gitlogtest.jira.Classes;
import it.matteociccaglione.gitlogtest.jira.Issue;
import it.matteociccaglione.gitlogtest.jira.JiraManager;
import it.matteociccaglione.gitlogtest.jira.Version;
import org.eclipse.jgit.api.errors.GitAPIException;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.ParseException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class SimpleClassForLogTest {
    public static void main(String[] args) throws IOException, ParseException, GitAPIException {
        /*
        System.out.println("This is the first commit");
        System.out.println("Added new print");
        System.out.println("Test");
        */
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
        Integer numberOfVersions = versions.size();
        Integer numberOfVersionToUse = numberOfVersions * 50 / 100;
        versions.sort(new Version.VersionComparator());
        List<Version> versionToUse = versions.subList(0,numberOfVersionToUse);
        for(Version version: versionToUse){
            System.out.println(version.toString());
        }
        List<Issue> bugs = JiraManager.retrieveIssues("ZOOKEEPER");
        GitLogMiningClass gitLog = GitLogMiningClass.getInstance("/home/utente/zookeeper/.git");
        //Now for each bug search commit with this bug id
        for (Issue bug: bugs){
            List<GitLogMiningClass.Commit> commits = gitLog.getCommits(bug.getId());
            Version affectedVersion = null;
            if(bug.getVersion()!=null){
                affectedVersion = bug.getVersion();
            }
            else{
                //Compute av using szz and proportion
                continue;
            }
            //For each commit I need to see what classes was modified
            for (GitLogMiningClass.Commit commit: commits){
                List<Classes> classes = gitLog.getFileModified(commit);
                /*
                Date commitDate = Date.from(Instant.ofEpochSecond(commit.getCommit().getCommitTime()));
                Version version = Version.getVersionByDate(versionToUse,commitDate);
                */
                affectedVersion.setClasses(classes,true);
            }
        }
        List<GitLogMiningClass.Commit> commits = gitLog.getCommits();
        for (GitLogMiningClass.Commit commit: commits){
            List<Classes> classes = gitLog.getFileModified(commit);
            Date commitDate = Date.from(Instant.ofEpochSecond(commit.getCommit().getCommitTime()));
            Version version = Version.getVersionByDate(versionToUse,commitDate);
            if(version == null){
                continue;
            }
            version.setClasses(classes,false);
        }
        FileWriter file = new FileWriter("/home/utente/Scrivania/zookeeper.csv");
        StringBuffer fileContent;
        for (Version ver: versionToUse){

            List<Classes> classes = ver.getClasses();
            if(classes==null){
                continue;
            }
            for (Classes clas: classes){
                fileContent = new StringBuffer();
                fileContent.append(ver.getVersionNumber()+",");
                fileContent.append(clas.getName()+","+clas.getLocTouched().toString()+","+clas.getLocAdded().toString()+","+clas.getChurn().toString()+","+clas.getBuggy().toString());
                fileContent.append("\n");
                file.write(fileContent.toString());
            }

        }
        file.close();
    }
}
