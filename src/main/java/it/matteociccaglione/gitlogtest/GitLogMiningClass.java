package it.matteociccaglione.gitlogtest;
import it.matteociccaglione.gitlogtest.jira.Classes;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.diff.*;
import org.eclipse.jgit.lib.ObjectReader;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.lib.RepositoryBuilder;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.treewalk.CanonicalTreeParser;
import org.eclipse.jgit.treewalk.EmptyTreeIterator;
import org.eclipse.jgit.util.io.DisabledOutputStream;


import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
public class GitLogMiningClass {
    private final ArrayList<RevCommit> commits = new ArrayList<>();
    private final String repositoryPath;
    private GitLogMiningClass(String repositoryPath) throws GitAPIException, IOException {
        this.repositoryPath = repositoryPath;
        Git git = this.buildRepository();
        Iterable<RevCommit> comms = git.log().call();
        for (RevCommit commit : comms){
            this.commits.add(commit);
        }
        git.close();
    }
    public static GitLogMiningClass getInstance(String repositoryPath) throws GitAPIException, IOException {

        return new GitLogMiningClass(repositoryPath);
    }
    private  Git buildRepository() throws IOException {
        Repository repo = new RepositoryBuilder().setGitDir(new File(repositoryPath)).setMustExist(true).build();
        return new Git(repo);
    }
    public  List<Commit> getCommits(String filter){
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
            this.actualCommit = commit;
            this.prevCommit = prevCommit;
        }

        public RevCommit getCommit() {
            return actualCommit;
        }

        public void setCommit(RevCommit commit) {
            this.actualCommit = commit;
        }

        public RevCommit getPrevCommit() {
            return prevCommit;
        }

        public void setPrevCommit(RevCommit prevCommit) {
            this.prevCommit = prevCommit;
        }

        private RevCommit actualCommit;
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
            if(!fileName.endsWith(".java") || fileName.contains("/test/") || diff.getChangeType() == DiffEntry.ChangeType.COPY){
                //ignore non .java files
                continue;
            }
            Classes cl = Classes.getClassByName(fileName,classes);
            if(cl==null){
                cl=new Classes(fileName);
                classes.add(cl);
            }
            else{
                cl.setName(diff.getNewPath());
            }
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

}
