package it.matteociccaglione.gitlogtest;
import it.matteociccaglione.gitlogtest.jira.Classes;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.diff.DiffFormatter;
import org.eclipse.jgit.diff.Edit;
import org.eclipse.jgit.diff.RawTextComparator;
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
    private String repositoryPath;
    private GitLogMiningClass(String repositoryPath){
        this.repositoryPath = repositoryPath;
    }
    public static GitLogMiningClass getInstance(String repositoryPath){
        return new GitLogMiningClass(repositoryPath);
    }
    private  Git buildRepository() throws IOException {
        Repository repo = new RepositoryBuilder().setGitDir(new File(repositoryPath)).setMustExist(true).build();
        Git git = new Git(repo);
        return git;
    }
    public  List<Commit> getCommits(String filter) throws GitAPIException, IOException {
        List<Commit> codes = new ArrayList<>();
        Git git = this.buildRepository();
        RevCommit prevCommit = null;
        /*
        for (Ref branch : branches) {
            System.out.println(branch.getName());
            if (branch.getName().equals("refs/heads/master")) {
                System.out.println("HERE");
                */
                Iterable<RevCommit> commits = git.log().call();
                for (RevCommit com : commits) {
                    String mex = com.getFullMessage();
                    if (mex.contains(filter)) {
                        codes.add(new Commit(com,prevCommit));
                    }
                    prevCommit = com;
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
        }

        return classes;
    }

}
