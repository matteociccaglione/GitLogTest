package it.matteociccaglione.gitlogtest.jira;

import java.util.List;

public class Issue {
    private String key;
    private String id;
    private List<Version> version;
    private String createdDate;
    private String resolvedDate;

    public Issue(String key, String id, List<Version> version, String createdDate, String resolvedDate) {
        this.key = key;
        this.id = id;
        this.version = version;
        this.createdDate = createdDate;
        this.resolvedDate = resolvedDate;
    }

    public String getKey() {
        return key;
    }

    public String getId() {
        return id;
    }

    public List<Version> getVersion() {
        return version;
    }

    public String getCreatedDate() {
        return createdDate;
    }

    public String getResolvedDate() {
        return resolvedDate;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setVersion(List<Version> version) {
        this.version = version;
    }

    public void setCreatedDate(String createdDate) {
        this.createdDate = createdDate;
    }

    public void setResolvedDate(String resolvedDate) {
        this.resolvedDate = resolvedDate;
    }
}
