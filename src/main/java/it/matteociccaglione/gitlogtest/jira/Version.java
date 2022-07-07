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
            this.classes = Classes.copyList(classes);
            return;
        }
        for (Classes cl : classes){
                Classes prevCl = modifyClassMetrics(cl,false);
                if(prevCl==null){
                    continue;
                }
                prevCl.setnFix(prevCl.getnFix() + cl.getnFix());

        }
    }
    private Classes modifyClassMetrics(Classes cl,Boolean buggy){
        Classes prevCl = Classes.getClassByName(cl.getName(),this.classes);
        if(prevCl==null){
            this.classes.add(cl.getCopy());
            if(Boolean.TRUE.equals(buggy))
                cl.setBuggy(true);
            return null;
        }
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
        return prevCl;
    }
    public void setClasses(List<Classes> classes,Boolean buggy) {
        if(this.classes == null){
            this.classes = Classes.copyList(classes);
            if(Boolean.TRUE.equals(buggy)){
                for (Classes cl: this.classes){
                    cl.setBuggy(true);
                }
            }
            return;
        }
        for (Classes cl : classes){
            Classes prevCl = modifyClassMetrics(cl,buggy);
            if(prevCl==null){
                continue;
            }
            if(Boolean.TRUE.equals(buggy)){
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
            return version.getVersionDate().compareTo(t1.getVersionDate());
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
            return versions.get(versions.size()-1);
        }
        return finalVersion;
    }
    public static Version getVersionByDateAfter(List<Version> versions, Date date){
        Version finalVersion = null;
        for (Version ver: versions){
            if(ver.versionDate.compareTo(date)>=0){
                if(finalVersion == null){
                    finalVersion=ver;
                    continue;
                }
                if(finalVersion.getVersionDate().compareTo(ver.getVersionDate())>0){
                    finalVersion = ver;
                }
            }
        }
        if(finalVersion==null){
            return versions.get(versions.size()-1);
        }
        return finalVersion;
    }

    public void setBuggyClasses(List<Classes> classes){
        if(this.classes == null){
            this.classes = Classes.copyList(classes);
                for (Classes cl: this.classes) {
                    cl.setBuggy(true);
                }
            return;
        }
        for (Classes cl : classes){
            Classes prevCl = Classes.getClassByName(cl.getName(),this.classes);
            if(prevCl==null){
                this.classes.add(cl.getCopy());
                prevCl=cl;
            }
            prevCl.setBuggy(true);
        }
    }
    public static float getPercentageDefective(List<Version> versions){
        float nDefective = 0;
        float nTotal = 0;
        for(Version ver: versions) {
            if(ver.classes==null){
                continue;
            }
            for (Classes cls : ver.classes) {
                if (Boolean.TRUE.equals(cls.getBuggy())) {
                    nDefective++;
                }
                nTotal++;
            }
        }
        if(nTotal==0)
            nTotal=1;
        return nDefective/nTotal;
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
    public static List<String> getPreviousVersionNumber(List<Version> versionToUse, Date versionDate){
        List<String> result = new ArrayList<>();
        for (Version ver: versionToUse){
            if(ver.getVersionDate().after(versionDate)){
                result.add(ver.getVersionNumber());
            }
        }
        return result;
    }
}
