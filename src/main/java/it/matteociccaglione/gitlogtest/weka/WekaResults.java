package it.matteociccaglione.gitlogtest.weka;

import weka.classifiers.Classifier;

public class WekaResults {
    private Double AUC;
    private Double recall;
    private Double precision;
    private Double kappa;
    private int nReleases;
    private Classifiers classifier;
    private float perTraining;
    private float perDefTraining;
    private float perDefTesting;
    private String balancing;
    private boolean featureSelection;
    private double tp;
    private double tn;
    private double fp;
    private double fn;
    private CostSensitiveType costSensitiveType;

    public CostSensitiveType getCostSensitiveType() {
        return costSensitiveType;
    }

    public void setCostSensitiveType(CostSensitiveType costSensitiveType) {
        this.costSensitiveType = costSensitiveType;
    }

    public float getPerTraining() {
        return perTraining;
    }

    public void setPerTraining(float perTraining) {
        this.perTraining = perTraining;
    }

    public float getPerDefTraining() {
        return perDefTraining;
    }

    public void setPerDefTraining(float perDefTraining) {
        this.perDefTraining = perDefTraining;
    }

    public float getPerDefTesting() {
        return perDefTesting;
    }

    public void setPerDefTesting(float perDefTesting) {
        this.perDefTesting = perDefTesting;
    }

    public String getBalancing() {
        return balancing;
    }

    public void setBalancing(String balancing) {
        this.balancing = balancing;
    }

    public boolean getFeatureSelection() {
        return featureSelection;
    }

    public void setFeatureSelection(boolean featureSelection) {
        this.featureSelection = featureSelection;
    }

    public  double getTp() {
        return tp;
    }

    public void setTp(double tp) {
        this.tp = tp;
    }

    public double getTn() {
        return tn;
    }

    public void setTn(double tn) {
        this.tn = tn;
    }

    public double getFp() {
        return fp;
    }

    public void setFp(double fp) {
        this.fp = fp;
    }

    public double getFn() {
        return fn;
    }

    public void setFn(double fn) {
        this.fn = fn;
    }

    public Classifiers getClassifier() {
        return classifier;
    }

    public void setClassifier(Classifiers classifier) {
        this.classifier = classifier;
    }

    public int getnReleases() {
        return nReleases;
    }

    public void setnReleases(int nReleases) {
        this.nReleases = nReleases;
    }

    public WekaResults(Double AUC, Double recall, Double precision, Double kappa, Classifiers classifier) {
        this.AUC = AUC;
        this.recall = recall;
        this.precision = precision;
        this.kappa = kappa;
        this.classifier = classifier;
        this.nReleases = 0;
    }

    public Double getAUC() {
        return AUC;
    }

    public void setAUC(Double AUC) {
        this.AUC = AUC;
    }

    public Double getRecall() {
        return recall;
    }

    public void setRecall(Double recall) {
        this.recall = recall;
    }

    public Double getPrecision() {
        return precision;
    }

    public void setPrecision(Double precision) {
        this.precision = precision;
    }

    public Double getKappa() {
        return kappa;
    }

    public void setKappa(Double kappa) {
        this.kappa = kappa;
    }
}
