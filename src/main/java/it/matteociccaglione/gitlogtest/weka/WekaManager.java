package it.matteociccaglione.gitlogtest.weka;

import it.matteociccaglione.gitlogtest.file.FileBuilder;
import it.matteociccaglione.gitlogtest.jira.Version;
import weka.classifiers.Evaluation;
import weka.classifiers.bayes.NaiveBayes;
import weka.classifiers.lazy.IBk;
import weka.classifiers.trees.RandomForest;
import weka.core.Instances;
import weka.core.converters.ConverterUtils;
import weka.gui.beans.DataSource;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class WekaManager {
    public static String toArff(List<Version> versions, String filename) throws IOException {
        String header = "Version,File,LOC_Touched,LOC_Added,Churn,NAuth,MaxLOC_Added,MaxChurn,AvgLOC_Added,AvgChurn,NFix,Nr,Buggy";
        FileBuilder fb = FileBuilder.build(filename,versions,header);
        StringBuilder sb = new StringBuilder();
        sb.append(filename, 0, filename.length()-3).append(".arff");
        fb.toFlat(sb.toString());
        File f = new File(filename);
        f.delete();
        return sb.toString();
    }

    public static WekaResults classifier(String training,String testing, Classifiers classifier) throws Exception {
        ConverterUtils.DataSource dataTraining = new ConverterUtils.DataSource(training);
        Instances iTraining = dataTraining.getDataSet();
        ConverterUtils.DataSource dataTesting = new ConverterUtils.DataSource(testing);
        Instances iTesting = dataTesting.getDataSet();
        weka.classifiers.Classifier cl = null;
        switch (classifier){
            case NaiveBayes:cl = new NaiveBayes();
            case Ibk: cl = new IBk();
            case RandomForest: cl = new RandomForest();
        }
        cl.buildClassifier(iTraining);
        Evaluation eval = new Evaluation(iTesting);
        eval.evaluateModel(cl,iTesting);
        return new WekaResults(eval.areaUnderROC(1), eval.recall(1),eval.precision(1),eval.kappa(),classifier);

    }
}
