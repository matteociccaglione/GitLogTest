package it.matteociccaglione.gitlogtest;
import it.matteociccaglione.gitlogtest.file.FileBuilder;
import it.matteociccaglione.gitlogtest.jira.Classes;
import it.matteociccaglione.gitlogtest.jira.Issue;
import it.matteociccaglione.gitlogtest.jira.JiraManager;
import it.matteociccaglione.gitlogtest.jira.Version;
import it.matteociccaglione.gitlogtest.weka.Classifiers;
import it.matteociccaglione.gitlogtest.weka.WekaManager;
import it.matteociccaglione.gitlogtest.weka.WekaResults;
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
    public static void main(String[] args) throws Exception {
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
        bugs.sort(new Issue.IssueComparator());
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
                for (Version v : affectedVersion){
                    v.setBuggyClasses(classes);
                }
            }
        }
        System.out.println("Start with av computation");
        for (Issue bug: bugs){
            List<GitLogMiningClass.Commit> commits = gitLog.getCommits(bug.getKey());
            List<Version> affectedVersion = proportion(versionToUse,bug,commits.get(0));
            if(affectedVersion==null){
                continue;
            }
            Date commitDate;
            for (GitLogMiningClass.Commit commit: commits){
                List<Classes> classes = gitLog.getFileModified(commit);
                if(classes.size() == 0){
                    continue;
                }
                commitDate = Date.from(Instant.ofEpochSecond(commit.getCommit().getCommitTime()));
                Version version = Version.getVersionByDate(affectedVersion,commitDate);
                assert version != null;
                version.setClasses(classes,true);
                for (Version v : affectedVersion){
                    v.setBuggyClasses(classes);
                }
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
        for (Version version: versionToUse){
            List<Classes> cls = version.getClasses();
            for (Classes cl: cls){
                cl.setAvgChurn((float) (cl.getChurn()/cl.getNr()));
                cl.setAvgLocAdded((float) (cl.getLocAdded()/cl.getNr()));
            }
        }
        String header = "Version,File,LOC_Touched,LOC_Added,Churn,NAuth,MaxLOC_Added,MaxChurn,AvgLOC_Added,AvgChurn,NFix,Nr,Buggy";
        FileBuilder fb = FileBuilder.build("/home/utente/Scrivania/zookeeper.csv",versionToUse,header);
        fb.toFlat("/home/utente/Scrivania/zookeeper.arff");

        System.out.println("Starting walk-forward");
        walkForward("/home/utente/Scrivania/zookeeper.csv",versionToUse);
    }
    private static List<Version> proportion(List<Version> versionToUse, Issue bug, GitLogMiningClass.Commit commit) throws ParseException {
        Date commitDate = Date.from(Instant.ofEpochSecond(commit.getCommit().getCommitTime()));
        Version fixedVersion = Version.getVersionByDate(versionToUse,commitDate);
        if(fixedVersion==null){
            return null;
        }
        List<Version> affectedVersion = new ArrayList<>();
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
        return affectedVersion;
    }
    private static void walkForward(String file, List<Version> versionToUse) throws Exception {

        List<List<Version>> trainingSets = new ArrayList<>();
        List<Version> testingSets = new ArrayList<>();
        List<Version> trainingVersion = new ArrayList<>();
        List<Version> testingVersion = new ArrayList<>();
        List<Issue> bugs = JiraManager.retrieveIssues("ZOOKEEPER");
        for (int i = 0; i < versionToUse.size()-1; i++){
            int trainingStart = i-1;
            int testingStart = i;
            if(trainingStart>=0){
                List<Version> trainingSet = new ArrayList<>();
                for (int j = trainingStart; j < testingStart; j++){
                    trainingSet.add(versionToUse.get(j).getCopyWithoutCommits());
                }
                bugs.sort(new Issue.IssueComparator());
                GitLogMiningClass gitLog = GitLogMiningClass.getInstance("/home/utente/zookeeper/.git");
                for (Issue bug : bugs){
                    Date bugDate = new SimpleDateFormat("yy-MM-dd").parse(bug.getResolvedDate().substring(0,11));
                    Date lastDate = trainingSet.get(testingStart-1).getVersionDate();
                    if(bugDate.after(lastDate)){
                        break;
                    }
                    List< GitLogMiningClass.Commit> commits = gitLog.getCommits(bug.getKey());
                    List<Version> affectedVersion = new ArrayList<>();
                    if(bug.getVersion()!=null){
                        List<Version> av = bug.getVersion();
                        for(Version a: av) {
                            for (Version ver : trainingSet) {
                                if (Objects.equals(ver.getVersionNumber(), a.getVersionNumber())) {
                                    affectedVersion.add(ver);
                                    ver.setNumberOfBugFixed(ver.getNumberOfBugFixed() + 1);
                                }
                            }
                        }
                    }
                    else{
                        affectedVersion = proportion(trainingSet,bug,commits.get(0));
                    }
                    for (GitLogMiningClass.Commit commit: commits){

                        List<Classes> classes = gitLog.getFileModified(commit);
                        if(classes.size() == 0){
                            continue;
                        }
                        Date commitDate = Date.from(Instant.ofEpochSecond(commit.getCommit().getCommitTime()));
                        Version version = Version.getVersionByDate(affectedVersion,commitDate);
                        assert version != null;
                        version.setClasses(classes,true);
                        for (Version v : affectedVersion){
                            v.setBuggyClasses(classes);
                        }
                    }
                }
                trainingSets.add(trainingSet);
            }
            testingSets.add(versionToUse.get(testingStart));
            List<WekaResults> nb = performWeka(Classifiers.NaiveBayes,trainingSets,testingSets);
            List<WekaResults> ibk = performWeka(Classifiers.Ibk, trainingSets, testingSets);
            List<WekaResults> rf = performWeka(Classifiers.RandomForest, trainingSets,testingSets);
            FileBuilder.buildWekaCsv(List.of(nb,ibk,rf),"Dataset,#TrainingRelease,Classifier,Precision,Recall,AUC,Kappa","ZOOKEEPER","/home/utente/Scrivania/zoookeeperWekaResult.csv");

        }

    }
    private static List<WekaResults> performWeka(Classifiers classifier, List<List<Version>> trainingSets, List<Version> testingSet) throws Exception {
        List<WekaResults> wr = new ArrayList<>();
        for (int i = 0; i<trainingSets.size(); i++){
            String trainingFile = WekaManager.toArff(trainingSets.get(i),"/home/utente/Scrivania/training.csv");
            String testingFile = WekaManager.toArff(List.of(testingSet.get(i)), "/home/utente/Scrivania/testing.csv");
            WekaResults wr1 = WekaManager.classifier(trainingFile,testingFile,classifier);
            wr1.setnReleases(trainingSets.get(i).size());
            wr.add(wr1);
        }
        return wr;
    }
}
