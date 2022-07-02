package it.matteociccaglione.gitlogtest.jira;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.*;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.sql.Array;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class JiraManager {
    private JiraManager(){

    }
    private static String readAll(Reader rd) throws IOException {
        StringBuilder sb = new StringBuilder();
        int cp;
        while ((cp = rd.read()) != -1) {
            sb.append((char) cp);
        }
        return sb.toString();
    }



    private static JSONObject readJsonFromUrl(String url) throws IOException, JSONException {
        try (InputStream is = new URL(url).openStream()) {
            BufferedReader rd = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8));
            String jsonText = readAll(rd);
            JSONObject json = new JSONObject(jsonText);
            is.close();
            return json;
        }
    }
    public static List<Issue> retrieveIssues(String projectName) throws IOException, ParseException {
        List<Issue> issues = new ArrayList<>();
        String constantUrl = "https://issues.apache.org/jira/rest/api/2/search?jql=project=%22"
                + projectName + "%22AND%22issueType%22=%22Bug%22AND(%22status%22=%22closed%22OR"
                + "%22status%22=%22resolved%22)AND%22resolution%22=%22fixed%22&fields=key,resolutiondate,versions,created&startAt=";
        System.out.println(constantUrl);
        int issueCount=0;
        int count=0;
        int totalIssues = 0;

        do{
            count = issueCount + 1000;
            String url = constantUrl + Integer.toString(issueCount) + "&maxResults=" + Integer.toString(count);
            JSONObject json = readJsonFromUrl(url);
            JSONArray jIssues = json.getJSONArray("issues");
            totalIssues = json.getInt("total");
            for (; issueCount < totalIssues && issueCount < count; issueCount++){

                String key = jIssues.getJSONObject(issueCount%1000).get("key").toString();
                String id = jIssues.getJSONObject(issueCount%1000).get("id").toString();
                JSONObject fields = jIssues.getJSONObject(issueCount%1000).getJSONObject("fields");
                if(!fields.has("resolutiondate")){
                    continue;
                }
                String resolutionDate = fields.getString("resolutiondate");
                String created = fields.getString("created");
                JSONArray versions = fields.getJSONArray("versions");
                List<Version> av = new ArrayList<>();
                Version version = null;
                if (!versions.isEmpty()){
                    for(int i = 0; i<versions.length(); i++) {
                        version = parseVersion(versions.getJSONObject(i));
                        av.add(version);
                    }
                }
                Issue is = new Issue(key,id,av,created,resolutionDate);
                issues.add(is);
            }

        }while(issueCount<totalIssues);
        return issues;
    }

    public static List<Version> retrieveVersions(String projectName) throws IOException, ParseException {
        List<Version> versions = new ArrayList<>();
        String url = "https://issues.apache.org/jira/rest/api/2/project/"+projectName+"/version";
        int versionCount = 0;
        int count = 0;
        int total = 0;
        while(true){
            count = count + versionCount;
            JSONObject json = readJsonFromUrl(url+"?maxResult=50&startAt="+count);
            JSONArray jVersions = json.getJSONArray("values");
            total = jVersions.length();
            for(versionCount = 0;versionCount<total;versionCount++){
                Version version = parseVersion(jVersions.getJSONObject(versionCount));
                if(version.getVersionDate()==null){
                    continue;
                }
                versions.add(version);
            }
            versionCount = total;
            if(json.getBoolean("isLast")){
                break;
            }
        }
        return versions;
    }
    private static Version parseVersion(JSONObject ver) throws ParseException {
        String name = ver.getString("name");
        Date releaseD = null;
        if(ver.has("releaseDate")) {
            String releaseDate = ver.getString("releaseDate");
           releaseD = new SimpleDateFormat("yyyy-MM-dd").parse(releaseDate);
        }
        Boolean released = ver.getBoolean("released");
        return new Version(name, releaseD, released);
    }
}
