package it.matteociccaglione.gitlogtest;
import it.matteociccaglione.gitlogtest.file.FileBuilder;
import it.matteociccaglione.gitlogtest.jira.Classes;
import it.matteociccaglione.gitlogtest.jira.Issue;
import it.matteociccaglione.gitlogtest.jira.JiraManager;
import it.matteociccaglione.gitlogtest.jira.Version;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.revwalk.RevCommit;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;

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
        int numberOfVersions = versions.size();
        int numberOfVersionToUse = numberOfVersions * 50 / 100;
        versions.sort(new Version.VersionComparator());
        List<Version> versionToUse = versions.subList(0,numberOfVersionToUse);
        for(Version version: versionToUse){
            System.out.println(version.toString());
        }
        List<Issue> bugs = JiraManager.retrieveIssues("ZOOKEEPER");
        GitLogMiningClass gitLog = GitLogMiningClass.getInstance("/home/utente/zookeeper/.git");
        //Now for each bug search commit with this bug id
        List<Issue> copyBugs = List.copyOf(bugs);
        for (Issue bug: copyBugs){

            List<Version> affectedVersion = new ArrayList<>();
            if(bug.getVersion()!=null){
                List<Version> av = bug.getVersion();
                int i = 0;
                for(Version a: av) {
                    for (Version ver : versionToUse) {
                        if (Objects.equals(ver.getVersionNumber(), a.getVersionNumber())) {
                            affectedVersion.add(ver);
                            ver.setNumberOfBugFixed(ver.getNumberOfBugFixed() + 1);
                        }
                    }
                }
                bugs.remove(bug);
            }

            else{
                continue;
            }
            if(affectedVersion.isEmpty()){
                continue;
            }
            List<GitLogMiningClass.Commit> commits = gitLog.getCommits(bug.getKey());
            //For each commit I need to see what classes was modified
            for (GitLogMiningClass.Commit commit: commits){

                List<Classes> classes = gitLog.getFileModified(commit);
                if(classes.size() == 0){
                    continue;
                }
                Date commitDate = Date.from(Instant.ofEpochSecond(commit.getCommit().getCommitTime()));
                Version version = Version.getVersionByDate(affectedVersion,commitDate);
                assert version != null;
                version.setClasses(classes,true);
            }
        }
        for (Issue bug: bugs){
            List<GitLogMiningClass.Commit> commits = gitLog.getCommits(bug.getKey());
            List<Version> affectedVersion = new ArrayList<>();
            Date commitDate = Date.from(Instant.ofEpochSecond(commits.get(0).getCommit().getCommitTime()));
            Version fixedVersion = Version.getVersionByDate(versionToUse,commitDate);
            if(fixedVersion==null){
                continue;
            }
            Integer numberOfFix = 0;
            int numberOfVer = 0;
            for (Version ver: versionToUse){
                if(ver.getVersionDate().equals(fixedVersion.getVersionDate())){
                    break;
                }
                numberOfFix+=ver.getNumberOfBugFixed();
                numberOfVer++;
            }
            float proportion = numberOfFix/ (float) numberOfVer;
            Date bugDate = new SimpleDateFormat("yy-MM-dd").parse(bug.getCreatedDate().substring(0,11));
            Version openingVersion = Version.getVersionByDate(versionToUse,bugDate);
            int ivEpoch = Math.round(Version.toEpochVersion(versionToUse,fixedVersion) - (Version.toEpochVersion(versionToUse,fixedVersion)-Version.toEpochVersion(versionToUse,openingVersion))*proportion);
            Version injectedVersion = versionToUse.get(ivEpoch);
            for (int i = ivEpoch; i < Version.toEpochVersion(versionToUse,fixedVersion); i++){
                affectedVersion.add(versionToUse.get(i));
            }
            for (GitLogMiningClass.Commit commit: commits){
                List<Classes> classes = gitLog.getFileModified(commit);
                if(classes.size() == 0){
                    continue;
                }
                commitDate = Date.from(Instant.ofEpochSecond(commit.getCommit().getCommitTime()));
                Version version = Version.getVersionByDate(affectedVersion,commitDate);
                assert version != null;
                version.setClasses(classes,true);
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
        Version prevVersion = null;
        for (Version version: versionToUse){
            if(prevVersion!=null){
                version.setClasses(prevVersion.getClasses());
            }
            prevVersion = version;
        }
        String header = "Version,File,LOC_Touched,LOC_Added,Churn,NAuth,MaxLOC_Added,MaxChurn,AvgLOC_Added,AvgChurn,NFix,Nr,Buggy";
        FileBuilder fb = FileBuilder.build("/home/utente/Scrivania/zookeeper.csv",versionToUse,header);
        fb.toFlat("/home/utente/Scrivania/zookeeper.arff");


    }
}
