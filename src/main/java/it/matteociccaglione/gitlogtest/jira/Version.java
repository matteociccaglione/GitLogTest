package it.matteociccaglione.gitlogtest.jira;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

public class Version {
    private String versionNumber;
    private Date versionDate;
    private Boolean released;

    public Boolean getReleased() {
        return released;
    }

    public void setReleased(Boolean released) {
        this.released = released;
    }

    public Version(String versionNumber, Date versionDate, Boolean released) {
        this.versionNumber = versionNumber;
        this.versionDate = versionDate;
        this.released = released;
    }

    public String getVersionNumber() {
        return versionNumber;
    }

    public void setVersionNumber(String versionNumber) {
        this.versionNumber = versionNumber;
    }

    public Date getVersionDate() {
        return versionDate;
    }

    public void setVersionDate(Date versionDate) {
        this.versionDate = versionDate;
    }

    @Override
    public String toString(){
        String verDate = "";
        if(versionDate != null){
            verDate = versionDate.toString();
        }
        else{
            verDate = "No release date for this version";
        }
        return "Version number: " + this.versionNumber + " version date: " + verDate + " released: " + released.toString();
    }
    public static class VersionComparator implements Comparator<Version> {

        @Override
        public int compare(Version version, Version t1) {
            String[] versionNumbersArray1 =version.versionNumber.split("\\.");
            String[] versionNumbersArray2 = t1.versionNumber.split("\\.");
            ArrayList<String> versionNumbers1 = new ArrayList<String>(List.of(versionNumbersArray1));
            ArrayList<String> versionNumbers2 = new ArrayList<String>(List.of(versionNumbersArray2));
            if(versionNumbers1.size()==2){
                versionNumbers1.add("0");
            }
            if(versionNumbers2.size()==2){
                versionNumbers2.add("0");
            }
            if(Integer.parseInt(versionNumbers1.get(0))>Integer.parseInt(versionNumbers2.get(0))){
                return 1;
            }
            if(Integer.parseInt(versionNumbers1.get(0))<Integer.parseInt(versionNumbers2.get(0))){
                return -1;
            }
            if(Integer.parseInt(versionNumbers1.get(1))>Integer.parseInt(versionNumbers2.get(1))){
                return 1;
            }
            if(Integer.parseInt(versionNumbers1.get(1))<Integer.parseInt(versionNumbers2.get(1))){
                return -1;
            }
            if(Integer.parseInt(versionNumbers1.get(2))>Integer.parseInt(versionNumbers2.get(2))){
                return 1;
            }
            if(Integer.parseInt(versionNumbers1.get(2))<Integer.parseInt(versionNumbers2.get(2))){
                return -1;
            }
            return 0;
        }

        @Override
        public boolean equals(Object o) {
            return false;
        }
    }
}
