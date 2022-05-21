package it.matteociccaglione.gitlogtest.weka;

import weka.classifiers.Classifier;

public class WekaResults {
    private Double AUC;
    private Double recall;
    private Double precision;
    private Double kappa;
    private int nReleases;
    private Classifiers classifier;

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
