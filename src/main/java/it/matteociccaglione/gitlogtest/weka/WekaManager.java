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
        f.delete();
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
    private static CostMatrix buildCostMatrix(){
        CostMatrix costMatrix = new CostMatrix(2);
        costMatrix.setCell(0, 0, 0.0);
        costMatrix.setCell(1, 0, (double) 10);
        costMatrix.setCell(0, 1, (double) 0);
        costMatrix.setCell(1, 1, 0.0);
        return costMatrix;
    }
    public static WekaResults sampling(Instances iTraining, Instances iTesting, CostSensitiveClassifier classifier, Classifiers classifiers, SamplingMethods samplingMethods,CostSensitiveType costSensitiveType) throws Exception {
        Classifier cl = classifier.getClassifier();
        if(costSensitiveType==CostSensitiveType.SENSITIVE){
            classifier.setCostMatrix(buildCostMatrix());
            classifier.setMinimizeExpectedCost(false);
        }
        if(costSensitiveType==CostSensitiveType.THRESHOLD){
            classifier.setCostMatrix(buildCostMatrix());
            classifier.setMinimizeExpectedCost(true);
        }
        Resample resample = new Resample();
        SpreadSubsample spreadSubsample = new SpreadSubsample();
        resample.setInputFormat(iTraining);
        FilteredClassifier fc = new FilteredClassifier();
        if(costSensitiveType==CostSensitiveType.NONE)
            fc.setClassifier(cl);
        else
            fc.setClassifier(classifier);
        if(samplingMethods == SamplingMethods.RESAMPLE){
            fc.setFilter(resample);
        }
        if(samplingMethods == SamplingMethods.SPREADSUBSAMPLE){
            String[] opts = new String[]{ "-M", "1.0"};
            spreadSubsample.setOptions(opts);
            fc.setFilter(spreadSubsample);
        }
        fc.buildClassifier(iTraining);
        Evaluation eval = new Evaluation(iTesting);
        eval.evaluateModel(fc,iTesting);
        WekaResults wr =  new WekaResults(eval.areaUnderROC(1), eval.recall(1),eval.precision(1),eval.kappa(),classifiers);
        wr.setTn(eval.numTrueNegatives(1));
        wr.setTp(eval.numTruePositives(1));
        wr.setFp(eval.numFalsePositives(1));
        wr.setFn(eval.numFalseNegatives(1));
        return wr;
    }

    public static List<Instances> featureSelection(String dataSource, boolean backward) throws Exception {
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
        Instances noFilterTesting = new ConverterUtils.DataSource(dataSource).getDataSet();
        Instances filteredTesting = Filter.useFilter(noFilterTesting,filter);
        filteredTesting.setClassIndex(filteredTraining.numAttributes()-1);
        return List.of(filteredTraining,filteredTesting);
    }
}
