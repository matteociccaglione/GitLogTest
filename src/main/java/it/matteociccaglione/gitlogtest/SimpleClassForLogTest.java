package it.matteociccaglione.gitlogtest;
import it.matteociccaglione.gitlogtest.file.FileBuilder;
import it.matteociccaglione.gitlogtest.jira.Classes;
import it.matteociccaglione.gitlogtest.jira.Issue;
import it.matteociccaglione.gitlogtest.jira.JiraManager;
import it.matteociccaglione.gitlogtest.jira.Version;
import it.matteociccaglione.gitlogtest.weka.*;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.revwalk.RevCommit;
import weka.classifiers.bayes.NaiveBayes;
import weka.classifiers.lazy.IBk;
import weka.classifiers.meta.CostSensitiveClassifier;
import weka.classifiers.trees.RandomForest;
import weka.core.Instances;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.*;

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
        createCsvForWekaPrediction("ZOOKEEPER","/home/utente/zookeeper/.git");
        createCsvForWekaPrediction("BOOKKEEPER","/home/utente/bookkeeper/.git");

        List<Version> versionToUseZ = buildProject("ZOOKEEPER","/home/utente/zookeeper/.git");
        List<Version> versionToUseB = buildProject("BOOKKEEPER","/home/utente/bookkeeper/.git");
        String filepathZ = "/home/utente/Scrivania/"+"ZOOKEEPER".toLowerCase()+".csv";
        String filepathB = "/home/utente/Scrivania/"+"BOOKKEEPER".toLowerCase()+".csv";
        walkForward(List.of(filepathZ,filepathB),List.of(versionToUseZ,versionToUseB),List.of("ZOOKEEPER","BOOKKEEPER"),List.of("/home/utente/zookeeper/.git","/home/utente/ISW2/bookkeeper/bookkeeper/.git"));

    }
    private  static void createCsvForWekaPrediction(String projectName, String projectPath) throws IOException, GitAPIException, ParseException {
        List<Version> versions = JiraManager.retrieveVersions(projectName);
        int numberOfVersions = versions.size();
        int numberOfVersionToUse = numberOfVersions;
        versions.sort(new Version.VersionComparator());
        List<Version> versionToUse = versions.subList(0,numberOfVersionToUse);
        for(Version version: versionToUse){
            System.out.println(version.toString());
        }
        List<Issue> bugs = JiraManager.retrieveIssues(projectName);
        bugs.sort(new Issue.IssueComparator());
        GitLogMiningClass gitLog = GitLogMiningClass.getInstance(projectPath);
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
                List<Classes> cls = version.getClasses();
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
            if(cls==null){
                continue;
            }
            for (Classes cl: cls){
                cl.setAvgChurn((float) (cl.getChurn()/cl.getNr()));
                cl.setAvgLocAdded((float) (cl.getLocAdded()/cl.getNr()));
            }
        }
        List<Version> copyVersions = new ArrayList<>();
        for (Version ver: versionToUse){
            copyVersions.add(ver);
        }
        for (Version ver: copyVersions){
            if(ver.getClasses()==null){
                versionToUse.remove(ver);
            }
        }
        String header = "Version,File,LOC_Touched,LOC_Added,Churn,NAuth,MaxLOC_Added,MaxChurn,AvgLOC_Added,AvgChurn,NFix,Nr,Buggy";
        FileBuilder fb = FileBuilder.build("/home/utente/Scrivania/"+projectName.toLowerCase()+".csv",versionToUse,header);
        fb.toFlat("/home/utente/Scrivania/"+projectName.toLowerCase()+".arff");
    }
    private static List<Version> buildProject(String projectName, String projectPath) throws Exception {
        List<Version> versions = JiraManager.retrieveVersions(projectName);
        int numberOfVersions = versions.size();
        int numberOfVersionToUse = numberOfVersions * 50 / 100;
        versions.sort(new Version.VersionComparator());
        List<Version> versionToUse = versions.subList(0,numberOfVersionToUse);
        for(Version version: versionToUse){
            System.out.println(version.toString());
        }
        List<Issue> bugs = JiraManager.retrieveIssues(projectName);
        bugs.sort(new Issue.IssueComparator());
        GitLogMiningClass gitLog = GitLogMiningClass.getInstance(projectPath);
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
                List<Classes> cls = version.getClasses();
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
            if(cls==null){
                continue;
            }
            for (Classes cl: cls){
                cl.setAvgChurn((float) (cl.getChurn()/cl.getNr()));
                cl.setAvgLocAdded((float) (cl.getLocAdded()/cl.getNr()));
            }
        }
        List<Version> copyVersions = new ArrayList<>();
        for (Version ver: versionToUse){
            copyVersions.add(ver);
        }
        for (Version ver: copyVersions){
            if(ver.getClasses()==null){
                versionToUse.remove(ver);
            }
        }
        String header = "Version,File,LOC_Touched,LOC_Added,Churn,NAuth,MaxLOC_Added,MaxChurn,AvgLOC_Added,AvgChurn,NFix,Nr,Buggy";
        FileBuilder fb = FileBuilder.build("/home/utente/Scrivania/"+projectName.toLowerCase()+".csv",versionToUse,header);
        fb.toFlat("/home/utente/Scrivania/"+projectName.toLowerCase()+".arff");

        return versionToUse;
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
    private static void walkForward(List<String> files, List<List<Version>> versions,List<String> projectNames, List<String> projectPaths) throws Exception {


        for (int c = 0; c < files.size(); c++) {
            List<Version> versionToUse = versions.get(c);
            String file = files.get(c);
            String projectName = projectNames.get(c);
            String projectPath = projectPaths.get(c);
            List<List<Version>> trainingSets = new ArrayList<>();
            Map<String,List<Version>> map = new HashMap<>();
            List<Version> trainingVersion = new ArrayList<>();
            List<Version> testingVersion = new ArrayList<>();
            List<Version> testingSets = new ArrayList<>();
            List<Issue> bugs = JiraManager.retrieveIssues(projectName);
            bugs.sort(new Issue.IssueComparator());

            for (int i = 0; i < versionToUse.size(); i++) {
                List<Version> trainingSet = new ArrayList<>();
                int testingStart = i;
                for (int j = 0; j < testingStart; j++) {
                    trainingSet.add(versionToUse.get(j).getCopyWithoutCommits());
                }
                trainingSets.add(trainingSet);
                testingSets.add(versionToUse.get(testingStart));
                map.put(versionToUse.get(testingStart).getVersionNumber(), trainingSet);
            }
            GitLogMiningClass gitLog = GitLogMiningClass.getInstance(projectPath);
            for (Issue bug : bugs) {
                Date bugDate = new SimpleDateFormat("yy-MM-dd").parse(bug.getResolvedDate().substring(0, 11));
                Version version = Version.getVersionByDate(versionToUse, bugDate);
                List<Version> trainingSet = null;
                if (map.containsKey(version.getVersionNumber())) {
                    trainingSet = map.get(version.getVersionNumber());
                }
                if (trainingSet == null) {
                    continue;
                }
                List<GitLogMiningClass.Commit> commits = gitLog.getCommits(bug.getKey());
                List<Version> affectedVersion = new ArrayList<>();
                if (bug.getVersion() != null) {
                    List<Version> av = bug.getVersion();
                    for (Version a : av) {
                        for (Version ver : trainingSet) {
                            if (Objects.equals(ver.getVersionNumber(), a.getVersionNumber())) {
                                affectedVersion.add(ver);
                                ver.setNumberOfBugFixed(ver.getNumberOfBugFixed() + 1);
                            }
                        }
                    }
                } else {
                    affectedVersion = proportion(trainingSet, bug, commits.get(0));
                }
                if (affectedVersion.isEmpty()) {
                    continue;
                }
                for (GitLogMiningClass.Commit commit : commits) {

                    List<Classes> classes = gitLog.getFileModified(commit);
                    if (classes.size() == 0) {
                        continue;
                    }
                    Date commitDate = Date.from(Instant.ofEpochSecond(commit.getCommit().getCommitTime()));
                    version = Version.getVersionByDate(affectedVersion, commitDate);
                    assert version != null;
                    version.setClasses(classes, true);
                    for (Version v : affectedVersion) {
                        v.setBuggyClasses(classes);
                    }
                }
            }
            List<GitLogMiningClass.Commit> commits = gitLog.getCommits();
            for (GitLogMiningClass.Commit commit : commits) {
                List<Classes> classes = gitLog.getFileModified(commit);
                if (classes.size() == 0) {
                    continue;
                }
                Date commitDate = Date.from(Instant.ofEpochSecond(commit.getCommit().getCommitTime()));
                Version version = Version.getVersionByDate(versionToUse, commitDate);
                for (int i = 0; i < trainingSets.size(); i++) {
                    for (int j = 0; j < trainingSets.get(i).size(); j++) {
                        if (version.getVersionNumber().equalsIgnoreCase(trainingSets.get(i).get(j).getVersionNumber())) {
                            trainingSets.get(i).get(j).setClasses(classes);
                        }
                    }
                }
            }
            List<WekaResults> nb = performWeka(Classifiers.NaiveBayes, trainingSets, testingSets, versionToUse, CostSensitiveType.SENSITIVE);
            List<WekaResults> ibk = performWeka(Classifiers.Ibk, trainingSets, testingSets, versionToUse, CostSensitiveType.SENSITIVE);
            List<WekaResults> rf = performWeka(Classifiers.RandomForest, trainingSets, testingSets, versionToUse, CostSensitiveType.SENSITIVE);
            List<WekaResults> nbT = performWeka(Classifiers.NaiveBayes, trainingSets, testingSets, versionToUse, CostSensitiveType.THRESHOLD);
            List<WekaResults> ibkT = performWeka(Classifiers.Ibk, trainingSets, testingSets, versionToUse, CostSensitiveType.THRESHOLD);
            List<WekaResults> rfT = performWeka(Classifiers.RandomForest, trainingSets, testingSets, versionToUse, CostSensitiveType.THRESHOLD);
            List<WekaResults> nbNf = performWeka(Classifiers.NaiveBayes, trainingSets, testingSets, versionToUse, CostSensitiveType.NONE);
            List<WekaResults> ibkNf = performWeka(Classifiers.Ibk, trainingSets, testingSets, versionToUse, CostSensitiveType.NONE);
            List<WekaResults> rfNf = performWeka(Classifiers.RandomForest, trainingSets, testingSets, versionToUse, CostSensitiveType.NONE);
            FileBuilder.buildWekaCsv(List.of(nb,ibk,rf,nbT,ibkT,rfT,nbNf,ibkNf,rfNf),"Dataset,#TrainingRelease,%Training,%Defective in training, %Defective in testing, Classifier,balancing, Feature selection,Cost sensitive, TP, FP, TN, FN,Precision,Recall,AUC,Kappa",projectName,"/home/utente/Scrivania/"+projectName+"WekaResult.csv");
        }
        }
    private static List<WekaResults> performWeka(Classifiers classifier, List<List<Version>> trainingSets, List<Version> testingSet, List<Version> totalData, CostSensitiveType costSensitiveType) throws Exception {
        List<WekaResults> wr = new ArrayList<>();

        for (int i = 1; i<trainingSets.size(); i++){
            weka.classifiers.Classifier cl = new NaiveBayes();
            if(classifier==Classifiers.Ibk){
                cl = new IBk();
            }
            if(classifier==Classifiers.NaiveBayes){
                cl = new NaiveBayes();
            }
            if(classifier == Classifiers.RandomForest){
                cl = new RandomForest();
            }
            String trainingFile = WekaManager.toArff(trainingSets.get(i),"/home/utente/Scrivania/training.csv");
            String testingFile = WekaManager.toArff(List.of(testingSet.get(i)), "/home/utente/Scrivania/testing.csv");
            WekaResults wr1;
            List<Instances> instances = WekaManager.featureSelection(trainingFile, true,testingFile);
            Instances testingInstances = instances.get(1);
            Instances trainingInstances = instances.get(0);
            CostSensitiveClassifier costSensitiveClassifier = new CostSensitiveClassifier();
            costSensitiveClassifier.setClassifier(cl);
            wr1 = WekaManager.sampling(trainingInstances, testingInstances, costSensitiveClassifier, classifier, SamplingMethods.SPREADSUBSAMPLE,costSensitiveType);
            //WekaResults wr1 = WekaManager.classifier(trainingFile,testingFile,cl,classifier);
            wr1.setnReleases(trainingSets.get(i).size());
            wr1.setPerTraining((float)trainingSets.get(i).size()/totalData.size());
            wr1.setPerDefTraining(Version.getPercentageDefective(trainingSets.get(i)));
            wr1.setPerDefTesting(Version.getPercentageDefective(List.of(testingSet.get(i))));
            wr1.setFeatureSelection(true);
            wr1.setCostSensitiveType(costSensitiveType);
            wr1.setBalancing("undersampling");
            wr.add(wr1);
        }
        return wr;
    }
    private static void computeIssues(List<Version> versionToUse, List<Issue> bugs){

    }
}
