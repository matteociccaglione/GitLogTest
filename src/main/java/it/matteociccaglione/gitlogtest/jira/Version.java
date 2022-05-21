package it.matteociccaglione.gitlogtest.jira;

import java.util.*;

public class Version {
    private String versionNumber;
    private Date versionDate;
    private Boolean released;
    private Integer numberOfBugFixed=0;

    public Integer getNumberOfBugFixed() {
        return numberOfBugFixed;
    }

    public void setNumberOfBugFixed(Integer numberOfBugFixed) {
        this.numberOfBugFixed = numberOfBugFixed;
    }


    private List<Classes> classes;

    public List<Classes> getClasses() {
        return classes;
    }
    public void setClasses(List<Classes> classes){
        if(this.classes == null){
            this.classes = classes;
            return;
        }
        for (Classes cl : classes){
            if(!this.classes.contains(cl)){
                this.classes.add(cl);
                continue;
            }
            Classes prevCl = Classes.getClassByName(cl.getName(),this.classes);
            prevCl.setLocTouched(prevCl.getLocTouched()+cl.getLocTouched());
            prevCl.setLocAdded(prevCl.getLocAdded()+cl.getLocAdded());
            prevCl.setChurn(prevCl.getChurn()+cl.getChurn());
            System.out.println(prevCl.getMaxChurn().toString());
            if(prevCl.getMaxChurn()<cl.getChurn()){
                prevCl.setMaxChurn(cl.getChurn());
            }
            System.out.println(prevCl.getMaxChurn().toString());
            if(prevCl.getMaxLocAdded()<cl.getLocAdded()){
                prevCl.setMaxLocAdded((long)cl.getLocAdded());
            }
            prevCl.setNr(prevCl.getNr()+cl.getNr());
            prevCl.setAuthors(cl.getAuthors());
            prevCl.setnFix(prevCl.getnFix()+cl.getnFix());
        }
    }
    public void setClasses(List<Classes> classes,Boolean buggy) {
        if(this.classes == null){
            this.classes = classes;
            if(buggy){
                for (Classes cl: this.classes){
                    cl.setBuggy(true);
                }
            }
            return;
        }
        for (Classes cl : classes){
            if(!this.classes.contains(cl)){
                this.classes.add(cl);
                continue;
            }
            Classes prevCl = Classes.getClassByName(cl.getName(),this.classes);
            prevCl.setLocTouched(prevCl.getLocTouched()+cl.getLocTouched());
            prevCl.setLocAdded(prevCl.getLocAdded()+cl.getLocAdded());
            prevCl.setChurn(prevCl.getChurn()+cl.getChurn());
            if(prevCl.getMaxChurn()<cl.getChurn()){
                prevCl.setMaxChurn(cl.getChurn());
            }
            if(prevCl.getMaxLocAdded()<cl.getLocAdded()){
                prevCl.setMaxLocAdded((long)cl.getLocAdded());
            }
            prevCl.setNr(prevCl.getNr()+cl.getNr());
            prevCl.setAuthors(cl.getAuthors());
            if(buggy){
                prevCl.setnFix(prevCl.getnFix()+cl.getnFix());
            }
            prevCl.setBuggy(buggy);
        }
    }

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
    public Version getCopyWithoutCommits(){
        return new Version(this.versionNumber,this.versionDate,this.released);
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
            /*
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
            return 0;*/
            return version.getVersionDate().compareTo(t1.getVersionDate());
        }

        @Override
        public boolean equals(Object o) {
            return false;
        }
    }
    public static Version getVersionByDate(List<Version> versions, Date date){
        Version finalVersion = null;
        for (Version ver: versions){
            if(ver.versionDate.compareTo(date)<0){
                if(finalVersion == null){
                    finalVersion=ver;
                    continue;
                }
                if(finalVersion.getVersionDate().compareTo(ver.getVersionDate())<0){
                    finalVersion = ver;
                }
            }
        }
        if(finalVersion==null){
            System.out.println(date.toString());
            for (Version ver: versions){
                System.out.println(ver.getVersionDate());
            }
            System.out.println("\n");
            return versions.get(versions.size()-1);
        }
        return finalVersion;
    }

    public void setBuggyClasses(List<Classes> classes){
        if(this.classes == null){
            this.classes = classes;
                for (Classes cl: this.classes) {
                    cl.setBuggy(true);
                }
            return;
        }
        for (Classes cl : classes){
            if(!this.classes.contains(cl)){
                this.classes.add(cl);
                continue;
            }
            Classes prevCl = Classes.getClassByName(cl.getName(),this.classes);
            prevCl.setBuggy(true);
        }
    }
    public static Integer toEpochVersion(List<Version> versions, Version version){
        int epochVersion = 0;
        for (Version ver: versions){
            if(Objects.equals(ver.getVersionNumber(), version.getVersionNumber())){
                return epochVersion;
            }
            epochVersion++;
        }
        return -1;
    }
}
