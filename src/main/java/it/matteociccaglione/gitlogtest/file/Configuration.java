package it.matteociccaglione.gitlogtest.file;

public class Configuration {
    private String projectName1;
    private String projectName2;
    private String projectPath1;
    private String projectPath2;

    public String getProjectOutputDirectory() {
        return projectOutputDirectory;
    }

    public void setProjectOutputDirectory(String projectOutputDirectory) {
        this.projectOutputDirectory = projectOutputDirectory;
    }

    private String projectOutputDirectory;
    public Configuration(String projectName1, String projectName2, String projectPath1, String projectPath2, String projectOutputDirectory) {
        this.projectName1 = projectName1;
        this.projectName2 = projectName2;
        this.projectPath1 = projectPath1;
        this.projectPath2 = projectPath2;
        this.projectOutputDirectory = projectOutputDirectory;
    }

    public String getProjectName1() {
        return projectName1;
    }

    public void setProjectName1(String projectName1) {
        this.projectName1 = projectName1;
    }

    public String getProjectName2() {
        return projectName2;
    }

    public void setProjectName2(String projectName2) {
        this.projectName2 = projectName2;
    }

    public String getProjectPath1() {
        return projectPath1;
    }

    public void setProjectPath1(String projectPath1) {
        this.projectPath1 = projectPath1;
    }

    public String getProjectPath2() {
        return projectPath2;
    }

    public void setProjectPath2(String projectPath2) {
        this.projectPath2 = projectPath2;
    }
}
