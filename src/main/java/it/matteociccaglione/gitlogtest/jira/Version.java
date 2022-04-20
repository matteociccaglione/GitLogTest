package it.matteociccaglione.gitlogtest.jira;

import java.util.Date;

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
}
