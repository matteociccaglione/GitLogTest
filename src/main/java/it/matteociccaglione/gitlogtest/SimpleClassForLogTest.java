package it.matteociccaglione.gitlogtest;
import it.matteociccaglione.gitlogtest.file.Configuration;
import it.matteociccaglione.gitlogtest.file.FileBuilder;
import it.matteociccaglione.gitlogtest.jira.Classes;
import it.matteociccaglione.gitlogtest.jira.Issue;
import it.matteociccaglione.gitlogtest.jira.JiraManager;
import it.matteociccaglione.gitlogtest.jira.Version;
import it.matteociccaglione.gitlogtest.weka.*;
import org.eclipse.jgit.api.errors.GitAPIException;

import weka.classifiers.bayes.NaiveBayes;
import weka.classifiers.lazy.IBk;
import weka.classifiers.trees.RandomForest;
import weka.core.Instances;


import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.*;

public class SimpleClassForLogTest {
    private static final String DATE_PATTERN = "yyyy-MM-dd";
    private static  Configuration conf;
    public static void main(String[] args) throws Exception {
        URL url = SimpleClassForLogTest.class.getClassLoader().getResource("configuration.csv");
        if(url!=null) {
            File configFile = new File(url.getFile());
            conf = FileBuilder.readConfiguration(configFile);


            List<Version> versionToUseZ = buildProject(conf.getProjectName1(), conf.getProjectPath1());
            List<Version> versionToUseB = buildProject(conf.getProjectName2(), conf.getProjectPath2());
            String filepathZ = conf.getProjectOutputDirectory() + conf.getProjectName1().toLowerCase() + ".csv";
            String filepathB = conf.getProjectOutputDirectory() + conf.getProjectName2().toLowerCase() + ".csv";
            walkForward(List.of(filepathZ, filepathB), List.of(versionToUseZ, versionToUseB), List.of(conf.getProjectName1(), conf.getProjectName2()), List.of(conf.getProjectPath1(), conf.getProjectPath2()));
        }

    }
    private static void searchAV(Issue bug, List<Version> versionToUse, List<Version> affectedVersion, List<Issue> bugs){
        if(bug.getVersion()!=null){
            List<Version> av = bug.getVersion();
            for(Version a: av) {
                for (Version ver : versionToUse) {
                    if (Objects.equals(ver.getVersionNumber(), a.getVersionNumber())) {
                        affectedVersion.add(ver);
                    }
                }
            }
            bugs.remove(bug);
        }
    }
    private static void computeBugWithAV(Issue bug,List<Version> versionToUse, List<Issue> bugs, GitLogMiningClass gitLog) throws GitAPIException, IOException, ParseException {
        Date fixedDate  = new SimpleDateFormat(DATE_PATTERN).parse(bug.getResolvedDate().substring(0,10));
        Version fixedVersion = Version.getVersionByDateAfter(versionToUse,fixedDate);
        fixedVersion.setNumberOfBugFixed(fixedVersion.getNumberOfBugFixed()+1);
        List<Version> affectedVersion = new ArrayList<>();
        searchAV(bug,versionToUse,affectedVersion,bugs);
        if(affectedVersion.isEmpty()){
            return;
        }
        List<GitLogMiningClass.Commit> commits = gitLog.getCommits(bug.getKey());
        //For each commit I need to see what classes was modified
        for (GitLogMiningClass.Commit commit: commits){

            List<Classes> classes = gitLog.getFileModified(commit);
            if(classes.isEmpty()){
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
    private static void computeAvg(List<Version> versionToUse){
        for (Version version: versionToUse){
            List<Classes> cls = version.getClasses();
            if(cls==null){
                continue;
            }
            for (Classes cl: cls){
                cl.setAvgChurn((float) ((double)cl.getChurn()/cl.getNr()));
                cl.setAvgLocAdded((float) ((double)cl.getLocAdded()/cl.getNr()));
            }
        }
    }
    private static void computeCommitWithoutBugs(List<GitLogMiningClass.Commit> commits, GitLogMiningClass gitLog, List<Version> versionToUse) throws GitAPIException, IOException {
        for (GitLogMiningClass.Commit commit: commits){
            List<Classes> classes = gitLog.getFileModified(commit);
            Date commitDate = Date.from(Instant.ofEpochSecond(commit.getCommit().getCommitTime()));
            Version version = Version.getVersionByDate(versionToUse,commitDate);
            if(version == null){
                continue;
            }
            version.setClasses(classes,false);
        }
    }


    private static List<Version> buildProject(String projectName, String projectPath) throws IOException, ParseException, GitAPIException {
        List<Version> versions = JiraManager.retrieveVersions(projectName);
        int numberOfVersions = versions.size();
        int numberOfVersionToUse = numberOfVersions * 50 / 100;
        versions.sort(new Version.VersionComparator());
        List<Version> versionToUse = versions.subList(0,numberOfVersionToUse);

        List<Issue> bugs = JiraManager.retrieveIssues(projectName);
        bugs.sort(new Issue.IssueComparator());
        GitLogMiningClass gitLog = GitLogMiningClass.getInstance(projectPath);
        //Now for each bug search commit with this bug id
        List<Issue> copyBugs = List.copyOf(bugs);
        for (Issue bug: copyBugs){
            computeBugWithAV(bug,versionToUse,bugs,gitLog);
        }
        for (Issue bug: bugs){
            Date fixedDate  = new SimpleDateFormat(DATE_PATTERN).parse(bug.getResolvedDate().substring(0,10));
            Version fixedVersion = Version.getVersionByDateAfter(versionToUse,fixedDate);
            fixedVersion.setNumberOfBugFixed(fixedVersion.getNumberOfBugFixed()+1);
            List<GitLogMiningClass.Commit> commits = gitLog.getCommits(bug.getKey());
            List<Version> affectedVersion = proportion(versionToUse,bug);
            if(affectedVersion.isEmpty()){
                continue;
            }
            Date commitDate;
            for (GitLogMiningClass.Commit commit: commits){
                List<Classes> classes = gitLog.getFileModified(commit);
                if(classes.isEmpty()){
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
        computeCommitWithoutBugs(commits,gitLog,versionToUse);
        computeAvg(versionToUse);
        List<Version> copyVersions = new ArrayList<>(versionToUse);
        for (Version ver: copyVersions){
            if(ver.getClasses()==null){
                versionToUse.remove(ver);
            }
        }
        String header = "Version,File,LOC_Touched,LOC_Added,Churn,NAuth,MaxLOC_Added,MaxChurn,AvgLOC_Added,AvgChurn,NFix,Nr,Buggy";
        FileBuilder fb = FileBuilder.build(conf.getProjectOutputDirectory()+projectName.toLowerCase()+".csv",versionToUse,header);
        fb.toFlat(conf.getProjectOutputDirectory()+projectName.toLowerCase()+".arff");

        return versionToUse;
    }
    private static List<Version> proportion(List<Version> versionToUse, Issue bug) throws ParseException {
        Date fixedDate  = new SimpleDateFormat(DATE_PATTERN).parse(bug.getResolvedDate().substring(0,10));
        Version fixedVersion = Version.getVersionByDateAfter(versionToUse,fixedDate);
        if(fixedVersion==null){
            return Collections.emptyList();
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
        if(numberOfVer==0)
            numberOfVer=1;
        float proportion = numberOfFix/ (float) numberOfVer;
        Date bugDate = new SimpleDateFormat(DATE_PATTERN).parse(bug.getCreatedDate().substring(0,11));
        Version openingVersion = Version.getVersionByDate(versionToUse,bugDate);
        int ivEpoch = Math.round(Version.toEpochVersion(versionToUse,fixedVersion) - (Version.toEpochVersion(versionToUse,fixedVersion)-Version.toEpochVersion(versionToUse,openingVersion))*proportion);
        for (int i = ivEpoch; i < Version.toEpochVersion(versionToUse,fixedVersion); i++){
            affectedVersion.add(versionToUse.get(i));
        }
        return affectedVersion;
    }

    private static List<Version> workWithVersion(Issue bug, List<Version> trainingSet) throws ParseException {
        Date fixedDate  = new SimpleDateFormat(DATE_PATTERN).parse(bug.getResolvedDate().substring(0,10));
        Version fixedVersion = Version.getVersionByDateAfter(trainingSet,fixedDate);
        fixedVersion.setNumberOfBugFixed(fixedVersion.getNumberOfBugFixed()+1);
        List<Version> affectedVersion = new ArrayList<>();
        if (bug.getVersion() != null) {
            List<Version> av = bug.getVersion();
            for (Version a : av) {
                for (Version ver : trainingSet) {
                    if (Objects.equals(ver.getVersionNumber(), a.getVersionNumber())) {
                        affectedVersion.add(ver);
                    }
                }
            }
        } else {
            affectedVersion = proportion(trainingSet, bug);
        }
        return affectedVersion;
    }
    private static void addTrainingSets(List<List<Version>> trainingSets, Map<String,List<Version>> map, Version version, List<Version> versionToUse){
        List<Version> trainingSet = map.get(version.getVersionNumber());
        trainingSets.add(trainingSet);
        List<String> otherVersionNumber = Version.getPreviousVersionNumber(versionToUse,version.getVersionDate());
        for (String num: otherVersionNumber){
            trainingSet=map.get(num);
            trainingSets.add(trainingSet);
        }
    }
    private static void computeCommitWF(Issue bug, List<Version> versionToUse, Map<String,List<Version>> map, GitLogMiningClass gitLog) throws GitAPIException, IOException, ParseException {
        Date bugDate = new SimpleDateFormat("yy-MM-dd").parse(bug.getResolvedDate().substring(0, 11));
        Version version = Version.getVersionByDate(versionToUse, bugDate);
        List<List<Version>> trainingSets = new ArrayList<>();
        List<Version> trainingSet = null;
        if (map.containsKey(version.getVersionNumber())) {
            addTrainingSets(trainingSets,map,version,versionToUse);
        }
        if (trainingSets.isEmpty()) {
            return;
        }
        int i = 0;
        List<GitLogMiningClass.Commit> commits = gitLog.getCommits(bug.getKey());
        while(i<trainingSets.size()) {
            trainingSet = trainingSets.get(i);
            if(trainingSet.isEmpty()){
                i++;
                continue;
            }
            List<Version> affectedVersion = workWithVersion(bug, trainingSet);

            if (affectedVersion.isEmpty()) {
                return;
            }
            for (GitLogMiningClass.Commit commit : commits) {

                List<Classes> classes = gitLog.getFileModified(commit);
                if (classes.isEmpty()) {
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
            i++;
        }
    }
    private static void computeCommitNoBugWF(GitLogMiningClass gitLog, List<Version> versionToUse,List<List<Version>> trainingSets) throws GitAPIException, IOException {
        List<GitLogMiningClass.Commit> commits = gitLog.getCommits();
        for (GitLogMiningClass.Commit commit : commits) {
            List<Classes> classes = gitLog.getFileModified(commit);
            if (classes.isEmpty()) {
                continue;
            }
            Date commitDate = Date.from(Instant.ofEpochSecond(commit.getCommit().getCommitTime()));
            Version version = Version.getVersionByDate(versionToUse, commitDate);
            for (List<Version> trainingSet : trainingSets) {
                for (Version value : trainingSet) {
                    if (version.getVersionNumber().equalsIgnoreCase(value.getVersionNumber())) {
                        value.setClasses(classes);
                    }
                }
            }
        }
    }
    private static void walkForward(List<String> files, List<List<Version>> versions,List<String> projectNames, List<String> projectPaths) throws Exception {


        for (int c = 0; c < files.size(); c++) {
            List<Version> versionToUse = versions.get(c);
            String projectName = projectNames.get(c);
            String projectPath = projectPaths.get(c);
            List<List<Version>> trainingSets = new ArrayList<>();
            Map<String, List<Version>> map = new HashMap<>();
            List<Version> testingSets = new ArrayList<>();
            List<Issue> bugs = JiraManager.retrieveIssues(projectName);
            bugs.sort(new Issue.IssueComparator());

            for (int i = 0; i < versionToUse.size(); i++) {
                List<Version> trainingSet = new ArrayList<>();
                for (int j = 0; j < i; j++) {
                    trainingSet.add(versionToUse.get(j).getCopyWithoutCommits());
                }
                trainingSets.add(trainingSet);
                testingSets.add(versionToUse.get(i));
                map.put(versionToUse.get(i).getVersionNumber(), trainingSet);
            }
            GitLogMiningClass gitLog = GitLogMiningClass.getInstance(projectPath);
            for (Issue bug : bugs) {
                computeCommitWF(bug, versionToUse, map, gitLog);
            }
            computeCommitNoBugWF(gitLog, versionToUse, trainingSets);

            List<WekaResults> nb = performWeka(Classifiers.NAIVE_BAYES, trainingSets, testingSets, versionToUse, CostSensitiveType.SENSITIVE_LEARNING);
            List<WekaResults> ibk = performWeka(Classifiers.IBK, trainingSets, testingSets, versionToUse, CostSensitiveType.SENSITIVE_LEARNING);
            List<WekaResults> rf = performWeka(Classifiers.RANDOM_FOREST, trainingSets, testingSets, versionToUse, CostSensitiveType.SENSITIVE_LEARNING);
            List<WekaResults> nbT = performWeka(Classifiers.NAIVE_BAYES, trainingSets, testingSets, versionToUse, CostSensitiveType.SENSITIVE_THRESHOLD);
            List<WekaResults> ibkT = performWeka(Classifiers.IBK, trainingSets, testingSets, versionToUse, CostSensitiveType.SENSITIVE_THRESHOLD);
            List<WekaResults> rfT = performWeka(Classifiers.RANDOM_FOREST, trainingSets, testingSets, versionToUse, CostSensitiveType.SENSITIVE_THRESHOLD);
            List<WekaResults> nbNf = performWeka(Classifiers.NAIVE_BAYES, trainingSets, testingSets, versionToUse, CostSensitiveType.NO_COST_SENSITIVE);
            List<WekaResults> ibkNf = performWeka(Classifiers.IBK, trainingSets, testingSets, versionToUse, CostSensitiveType.NO_COST_SENSITIVE);
            List<WekaResults> rfNf = performWeka(Classifiers.RANDOM_FOREST, trainingSets, testingSets, versionToUse, CostSensitiveType.NO_COST_SENSITIVE);
            FileBuilder.buildWekaCsv(List.of(nb, ibk, rf, nbT, ibkT, rfT, nbNf, ibkNf, rfNf), "Dataset,#TrainingRelease,%Training,%Defective in training, %Defective in testing, Classifier,balancing, Feature selection,Cost sensitive, TP, FP, TN, FN,Precision,Recall,AUC,Kappa", projectName, "/home/utente/Scrivania/" + projectName + "WekaResult.csv");
        }
    }
    private static List<WekaResults> performWeka(Classifiers classifier, List<List<Version>> trainingSets, List<Version> testingSet, List<Version> totalData, CostSensitiveType costSensitiveType) throws Exception {
        List<WekaResults> wr = new ArrayList<>();

        for (int i = 1; i<trainingSets.size(); i++){
            weka.classifiers.Classifier cl = new NaiveBayes();
            if(classifier==Classifiers.IBK){
                cl = new IBk();
            }
            if(classifier==Classifiers.NAIVE_BAYES){
                cl = new NaiveBayes();
            }
            if(classifier == Classifiers.RANDOM_FOREST){
                cl = new RandomForest();
            }
            String trainingFile = WekaManager.toArff(trainingSets.get(i),"/home/utente/Scrivania/training.csv");
            WekaResults wr1;
            String testingFile = WekaManager.toArff(List.of(testingSet.get(i)),"/home/utente/Scrivania/testing.csv");
            List<Instances> instances = WekaManager.featureSelection(trainingFile,testingFile, true);
            Instances testingInstances = instances.get(1);
            Instances trainingInstances = instances.get(0);
            testingInstances.setClassIndex(trainingInstances.numAttributes()-1);

            wr1 = WekaManager.sampling(trainingInstances, testingInstances, cl, classifier, SamplingMethods.SPREADSUBSAMPLE,costSensitiveType);
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
}
