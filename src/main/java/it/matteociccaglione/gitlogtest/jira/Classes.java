package it.matteociccaglione.gitlogtest.jira;

import java.util.ArrayList;
import java.util.List;

public class Classes {
    private String name;
    private Version version;
    private Boolean buggy;
    private Long size;
    private Long locTouched;
    private Integer nr;
    private Integer nFix;
    private Integer nAuth;
    private Integer locAdded;
    private Long maxLocAdded;
    private Float avgLocAdded;
    private Integer churn;
    private Integer maxChurn;
    private Float avgChurn;
    private List<String> authors = new ArrayList<>();
    public List<String> getAuthors(){
        return this.authors;
    }
    public void setAuthors(List<String> auth){
        for (String au : auth){
            if(!this.authors.contains(au)){
                this.authors.add(au);
            }
        }
    }
    public Classes(Version version, Boolean buggy, Long size, Long locTouched, Integer nr, Integer nFix, Integer nAuth, Integer locAdded, Long maxLocAdded, Float avgLocAdded, Integer churn, Integer maxChurn, Float avgChurn) {
        this.version = version;
        this.buggy = buggy;
        this.size = size;
        this.locTouched = locTouched;
        this.nr = nr;
        this.nFix = nFix;
        this.nAuth = nAuth;
        this.locAdded = locAdded;
        this.maxLocAdded = maxLocAdded;
        this.avgLocAdded = avgLocAdded;
        this.churn = churn;
        this.maxChurn = maxChurn;
        this.avgChurn = avgChurn;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Classes(String name){
        this.name = name;
        this.locAdded=0;
        this.locTouched = 0L;
        this.maxChurn = 0;
        this.avgChurn = 0f;
        this.avgLocAdded = 0f;
        this.nAuth = 0;
        this.nr = 0;
        this.nFix = 0;
        this.size = 0L;
        this.maxLocAdded = 0L;
        this.churn = 0;
    }
    public Version getVersion() {
        return version;
    }

    public void setVersion(Version version) {
        this.version = version;
    }

    public Boolean getBuggy() {
        return buggy;
    }

    public void setBuggy(Boolean buggy) {
        this.buggy = buggy;
    }

    public Long getSize() {
        return size;
    }

    public void setSize(Long size) {
        this.size = size;
    }

    public Long getLocTouched() {
        return locTouched;
    }

    public void setLocTouched(Long locTouched) {
        this.locTouched = locTouched;
    }

    public Integer getNr() {
        return nr;
    }

    public void setNr(Integer nr) {
        this.nr = nr;
    }

    public Integer getnFix() {
        return nFix;
    }

    public void setnFix(Integer nFix) {
        this.nFix = nFix;
    }

    public Integer getnAuth() {
        return this.authors.size();
    }

    public void setnAuth(Integer nAuth) {
        this.nAuth = nAuth;
    }

    public Integer getLocAdded() {
        return locAdded;
    }

    public void setLocAdded(Integer locAdded) {
        this.locAdded = locAdded;
    }

    public Long getMaxLocAdded() {
        return maxLocAdded;
    }

    public void setMaxLocAdded(Long maxLocAdded) {
        this.maxLocAdded = maxLocAdded;
    }

    public Float getAvgLocAdded() {
        return avgLocAdded;
    }

    public void setAvgLocAdded(Float avgLocAdded) {
        this.avgLocAdded = avgLocAdded;
    }

    public Integer getChurn() {
        return churn;
    }

    public void setChurn(Integer churn) {
        this.churn = churn;
    }

    public Integer getMaxChurn() {
        return maxChurn;
    }

    public void setMaxChurn(Integer maxChurn) {
        this.maxChurn = maxChurn;
    }

    public Float getAvgChurn() {
        return avgChurn;
    }

    public void setAvgChurn(Float avgChurn) {
        this.avgChurn = avgChurn;
    }
    public static Classes getClassByName(String name, List<Classes> classes){
        for (Classes cl : classes){
            if(cl.getName().equalsIgnoreCase(name)){
                return cl;
            }
        }
        return null;
    }
}
