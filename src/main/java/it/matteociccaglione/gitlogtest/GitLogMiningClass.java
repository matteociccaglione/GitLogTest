package it.matteociccaglione.gitlogtest;
import it.matteociccaglione.gitlogtest.jira.Classes;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.blame.BlameResult;
import org.eclipse.jgit.diff.*;
import org.eclipse.jgit.errors.IncorrectObjectTypeException;
import org.eclipse.jgit.errors.MissingObjectException;
import org.eclipse.jgit.lib.ObjectReader;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.lib.RepositoryBuilder;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.eclipse.jgit.treewalk.CanonicalTreeParser;
import org.eclipse.jgit.treewalk.EmptyTreeIterator;
import org.eclipse.jgit.util.io.DisabledOutputStream;


import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
public class GitLogMiningClass {
    private ArrayList<RevCommit> commits = new ArrayList<>();
    private String repositoryPath;
    private GitLogMiningClass(String repositoryPath) throws GitAPIException, IOException {
        this.repositoryPath = repositoryPath;
        Git git = this.buildRepository();

        /*
        for (Ref branch : branches) {
            System.out.println(branch.getName());
            if (branch.getName().equals("refs/heads/master")) {
                System.out.println("HERE");
                */
        Iterable<RevCommit> commits = git.log().call();
        for (RevCommit commit : commits){
            this.commits.add(commit);
        }
        git.close();
    }
    public static GitLogMiningClass getInstance(String repositoryPath) throws GitAPIException, IOException {

        return new GitLogMiningClass(repositoryPath);
    }
    private  Git buildRepository() throws IOException {
        Repository repo = new RepositoryBuilder().setGitDir(new File(repositoryPath)).setMustExist(true).build();
        Git git = new Git(repo);
        return git;
    }
    public  List<Commit> getCommits(String filter) throws GitAPIException, IOException {
        List<Commit> codes = new ArrayList<>();
        List<RevCommit> commitToRemove = new ArrayList<>();
        RevCommit prevCommit = null;
                for (RevCommit com : this.commits) {
                    String mex = com.getFullMessage();
                    if (mex.contains(filter)) {
                        codes.add(new Commit(com,prevCommit));
                        commitToRemove.add(com);
                    }
                    prevCommit = com;
                }
                for (RevCommit com: commitToRemove){
                    this.commits.remove(com);
                }
                return codes;
    }
    public List<Commit> getCommits(){
        List<Commit> codes = new ArrayList<>();
        List<RevCommit> commitToRemove = new ArrayList<>();
        RevCommit prevCommit = null;
        for (RevCommit com : this.commits) {
            codes.add(new Commit(com,prevCommit));
            commitToRemove.add(com);
            prevCommit = com;
        }
        for (RevCommit com: commitToRemove){
            this.commits.remove(com);
        }
        return codes;
    }
    public static class Commit{
        public Commit(RevCommit commit, RevCommit prevCommit) {
            this.commit = commit;
            this.prevCommit = prevCommit;
        }

        public RevCommit getCommit() {
            return commit;
        }

        public void setCommit(RevCommit commit) {
            this.commit = commit;
        }

        public RevCommit getPrevCommit() {
            return prevCommit;
        }

        public void setPrevCommit(RevCommit prevCommit) {
            this.prevCommit = prevCommit;
        }

        private RevCommit commit;
        private RevCommit prevCommit;

    }
    public  List<Classes> getFileModified(Commit commit) throws IOException, GitAPIException {
        Git git = buildRepository();
        List<DiffEntry> differences = null;
        List<Classes> classes = new ArrayList<>();
        ObjectReader reader = git.getRepository().newObjectReader();
        CanonicalTreeParser newParser = new CanonicalTreeParser(null,reader,commit.getCommit().getTree().getId());
        if(commit.prevCommit == null) {
            EmptyTreeIterator oldIterator = new EmptyTreeIterator();
            differences = git.diff().setOldTree(oldIterator).setNewTree(newParser).call();
        }
        else{
            CanonicalTreeParser oldIterator = new CanonicalTreeParser(null,reader,commit.getPrevCommit().getTree().getId());
            differences = git.diff().setOldTree(oldIterator).setNewTree(newParser).call();
        }
        DiffFormatter df = new DiffFormatter(DisabledOutputStream.INSTANCE);
        df.setRepository(git.getRepository());
        df.setDiffComparator(RawTextComparator.DEFAULT);
        df.setDetectRenames(true);
        for (DiffEntry diff : differences){
            String fileName = diff.getOldPath();
            if(fileName.equalsIgnoreCase("/dev/null")){
                //This is a new file
                fileName = diff.getNewPath();
            }
            if(!fileName.endsWith(".java")){
                //ignore non .java files
                continue;
            }
            Classes cl = new Classes(fileName);
            classes.add(cl);
            Integer linesDeleted = 0;
            Integer linesAdded = 0;
            for (Edit edit : df.toFileHeader(diff).toEditList()) {
                linesDeleted += edit.getEndA() - edit.getBeginA();
                linesAdded += edit.getEndB() - edit.getBeginB();
            }
            cl.setLocAdded(cl.getLocAdded()+linesAdded);
            cl.setLocTouched(cl.getLocTouched()+linesAdded+linesDeleted);
            cl.setChurn(cl.getChurn()+linesAdded-linesDeleted);
            cl.setAuthors(List.of(commit.getCommit().getAuthorIdent().getName()));
            cl.setMaxChurn(cl.getMaxChurn());
            cl.setMaxLocAdded((long)cl.getLocAdded());
            cl.setNr(1);
            cl.setnFix(1);

        }
        git.close();
        return classes;
    }

    public RevCommit blame(String file, RevCommit commit) throws IOException, GitAPIException {
        Git git = buildRepository();
        BlameResult blameResult = git.blame().setFilePath(file).setTextComparator(RawTextComparator.WS_IGNORE_ALL).call();
        RawText rawText = blameResult.getResultContents();
        RevCommit result = null;
        for (int i = 0; i < rawText.size(); i++){
            RevCommit sourceCommit = blameResult.getSourceCommit(i);
            if(sourceCommit.getId() == commit.getId()){
                break;
            }
            result = sourceCommit;
        }
        return result;
    }
}
