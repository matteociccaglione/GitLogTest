package it.matteociccaglione.gitlogtest.weka;

import it.matteociccaglione.gitlogtest.file.FileBuilder;
import it.matteociccaglione.gitlogtest.jira.Version;
import weka.classifiers.Classifier;
import weka.classifiers.CostMatrix;
import weka.classifiers.meta.CostSensitiveClassifier;
import weka.classifiers.meta.FilteredClassifier;
import weka.filters.Filter;
import weka.filters.supervised.attribute.AttributeSelection;
import weka.attributeSelection.CfsSubsetEval;
import weka.attributeSelection.GreedyStepwise;
import weka.classifiers.Evaluation;
import weka.core.Instances;
import weka.core.converters.ConverterUtils;
import weka.filters.supervised.instance.Resample;
import weka.filters.supervised.instance.SpreadSubsample;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;


public class WekaManager {
    private WekaManager(){

    }
    public static String toArff(List<Version> versions, String filename) throws IOException {
        String header = "Version,File,LOC_Touched,LOC_Added,Churn,NAuth,MaxLOC_Added,MaxChurn,AvgLOC_Added,AvgChurn,NFix,Nr,Buggy";
        FileBuilder fb = FileBuilder.build(filename,versions,header);
        StringBuilder sb = new StringBuilder();
        sb.append(filename, 0, filename.length()-4).append(".arff");
        fb.toFlat(sb.toString());
        File f = new File(filename);
        Files.delete(f.toPath());
        return sb.toString();
    }

    public static WekaResults classifier(String training,String testing, weka.classifiers.Classifier classifier, Classifiers classifiers) throws Exception {
        ConverterUtils.DataSource dataTraining = new ConverterUtils.DataSource(training);
        Instances iTraining = dataTraining.getDataSet();
        ConverterUtils.DataSource dataTesting = new ConverterUtils.DataSource(testing);
        Instances iTesting = dataTesting.getDataSet();
        return WekaManager.classifier(iTraining,iTesting,classifier,classifiers);

    }
    public static WekaResults classifier(Instances iTraining, Instances iTesting, weka.classifiers.Classifier classifier, Classifiers classifiers) throws Exception {
        iTraining.setClassIndex(iTraining.numAttributes()-1);
        classifier.buildClassifier(iTraining);

        iTesting.setClassIndex(iTesting.numAttributes()-1);
        Evaluation eval = new Evaluation(iTesting);
        eval.evaluateModel(classifier,iTesting);
        return new WekaResults(eval.areaUnderROC(1), eval.recall(1),eval.precision(1),eval.kappa(),classifiers);
    }

    public static WekaResults sampling(String training, String testing, weka.classifiers.Classifier classifier, Classifiers classifiers, SamplingMethods samplingMethods, CostSensitiveType costSensitiveType) throws Exception {
        ConverterUtils.DataSource dataTraining = new ConverterUtils.DataSource(training);
        Instances iTraining = dataTraining.getDataSet();
        iTraining.setClassIndex(iTraining.numAttributes()-1);
        ConverterUtils.DataSource dataTesting = new ConverterUtils.DataSource(testing);
        Instances iTesting = dataTesting.getDataSet();
        iTesting.setClassIndex(iTesting.numAttributes()-1);
        CostSensitiveClassifier cl = new CostSensitiveClassifier();
        cl.setClassifier(classifier);
        return sampling(iTraining,iTesting,cl,classifiers,samplingMethods,costSensitiveType);
    }
    private static CostMatrix buildCostMatrix(double fp, double fn){
        CostMatrix costMatrix = new CostMatrix(2);
        costMatrix.setCell(0, 0, 0.0);
        costMatrix.setCell(1, 0,  fp);
        costMatrix.setCell(0, 1,  fn);
        costMatrix.setCell(1, 1, 0.0);
        return costMatrix;
    }
    public static WekaResults sampling(Instances iTraining, Instances iTesting, Classifier cl, Classifiers classifiers, SamplingMethods samplingMethods,CostSensitiveType costSensitiveType) throws Exception {
        CostSensitiveClassifier classifier = new CostSensitiveClassifier();
        if(costSensitiveType==CostSensitiveType.SENSITIVE_LEARNING){
            classifier.setCostMatrix(buildCostMatrix(10,1));
            classifier.setMinimizeExpectedCost(false);
        }
        if(costSensitiveType==CostSensitiveType.SENSITIVE_THRESHOLD){
            classifier.setCostMatrix(buildCostMatrix(10,1));
            classifier.setMinimizeExpectedCost(true);
        }
        Resample resample = new Resample();
        SpreadSubsample spreadSubsample = new SpreadSubsample();
        resample.setInputFormat(iTraining);
        FilteredClassifier fc = new FilteredClassifier();
        fc.setClassifier(cl);


        if(samplingMethods == SamplingMethods.RESAMPLE){
            fc.setFilter(resample);
        }
        if(samplingMethods == SamplingMethods.SPREADSUBSAMPLE){
            String[] opts = new String[]{ "-M", "1.0"};
            spreadSubsample.setOptions(opts);
            fc.setFilter(spreadSubsample);
        }
        classifier.setClassifier(fc);
        Evaluation eval = new Evaluation(iTesting);
        if(costSensitiveType==CostSensitiveType.NO_COST_SENSITIVE){
            fc.buildClassifier(iTraining);
            eval.evaluateModel(fc,iTesting);
        }
        else {
            classifier.buildClassifier(iTraining);
            eval.evaluateModel(classifier,iTesting);
        }
        WekaResults wr =  new WekaResults(eval.areaUnderROC(1), eval.recall(1),eval.precision(1),eval.kappa(),classifiers);
        wr.setTn(eval.numTrueNegatives(1));
        wr.setTp(eval.numTruePositives(1));
        wr.setFp(eval.numFalsePositives(1));
        wr.setFn(eval.numFalseNegatives(1));
        return wr;
    }

    public static List<Instances> featureSelection(String dataSource,String testingSource, boolean backward) throws Exception {
        ConverterUtils.DataSource dataTraining = new ConverterUtils.DataSource(dataSource);
        Instances iTraining = dataTraining.getDataSet();
        iTraining.setClassIndex(iTraining.numAttributes()-1);
        CfsSubsetEval eval = new CfsSubsetEval();
        AttributeSelection filter = new AttributeSelection();
        GreedyStepwise search = new GreedyStepwise();
        search.setSearchBackwards(backward);
        filter.setEvaluator(eval);
        filter.setSearch(search);
        filter.setInputFormat(iTraining);
        Instances filteredTraining = Filter.useFilter(iTraining,filter);
        filteredTraining.setClassIndex(filteredTraining.numAttributes()-1);
        Instances noFilterTesting = new ConverterUtils.DataSource(testingSource).getDataSet();
        Instances filteredTesting = Filter.useFilter(noFilterTesting,filter);
        filteredTesting.setClassIndex(filteredTraining.numAttributes()-1);
        return List.of(filteredTraining,filteredTesting);
    }
    public static Instances getInstancesFromSource(String dataSource) throws Exception {
        ConverterUtils.DataSource dataTraining = new ConverterUtils.DataSource(dataSource);
        return dataTraining.getDataSet();
    }
}
