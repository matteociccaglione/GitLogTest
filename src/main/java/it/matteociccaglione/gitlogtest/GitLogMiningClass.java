package it.matteociccaglione.gitlogtest;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.errors.IncorrectObjectTypeException;
import org.eclipse.jgit.errors.MissingObjectException;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.lib.RepositoryBuilder;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;


import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
public class GitLogMiningClass {
    public static List<String> getCommits(String filter) throws GitAPIException, IOException {
        System.out.println("Method invoked");
        List<String> codes = new ArrayList<>();
        Repository repo = new RepositoryBuilder().setGitDir(new File("/home/utente/IdeaProjects/GitLogTest/.git")).setMustExist(true).build();
        System.out.println("repo loaded");
        Git git = new Git(repo);
        /*
        for (Ref branch : branches) {
            System.out.println(branch.getName());
            if (branch.getName().equals("refs/heads/master")) {
                System.out.println("HERE");
                */
                Iterable<RevCommit> commits = git.log().call();
                for (RevCommit com : commits) {
                    String mex = com.getFullMessage();
                    System.out.println(mex);
                    if (mex.contains(filter)) {
                        codes.add(com.getId().getName());
                    }
                }
                return codes;
    }
}
