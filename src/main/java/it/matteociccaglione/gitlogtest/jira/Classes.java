package it.matteociccaglione.gitlogtest.jira;

import java.util.ArrayList;
import java.util.List;

public class Classes {
    private List<String> names= new ArrayList<>();
    private String name;
    private Version version;
    private Boolean buggy;
    private Long size;
    private Long locTouched;
    private Integer nr;
    private Integer nFix;
    private Integer locAdded;
    private Long maxLocAdded;
    private Float avgLocAdded;
    private Integer churn;
    private Integer maxChurn;
    private Float avgChurn;
    private final List<String> authors = new ArrayList<>();
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


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.names.add(name);
    }

    public Classes getCopy(){
        Classes cl = new  Classes(this.name);
        cl.locAdded=this.locAdded;
        cl.locTouched=this.locTouched;
        cl.maxChurn=this.maxChurn;
        cl.avgChurn=this.avgChurn;
        cl.avgLocAdded=this.avgLocAdded;
        cl.nr=this.nr;
        cl.nFix=this.nFix;
        cl.size=this.size;
        cl.maxLocAdded=this.maxLocAdded;
        cl.churn=this.churn;
        cl.buggy=this.buggy;
        cl.names=this.names;
        return cl;
    }
    public static List<Classes> copyList(List<Classes> source){
        List<Classes> dest = new ArrayList<>();
        for (Classes cl: source){
            dest.add(cl.getCopy());
        }
        return dest;
    }

    public Classes(String name){
        this.name = name;
        this.locAdded=0;
        this.locTouched = 0L;
        this.maxChurn = 0;
        this.avgChurn = 0f;
        this.avgLocAdded = 0f;
        this.nr = 0;
        this.nFix = 0;
        this.size = 0L;
        this.maxLocAdded = 0L;
        this.churn = 0;
        this.buggy = false;
        this.names.add(name);
    }
    public List<String> getNames(){
        return this.names;
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
            if(cl.getNames().contains(name)){
                return cl;
            }
        }
        return null;
    }
}
