/**
 *
 */
package gitlet;

import static gitlet.ReferenceType.HEAD;

/**
 * @author Winston Represents a log command which logs all of the commits
 *         starting from the head.
 */
public class LogCommand implements CommandInterface {

    @Override
    public void runCommand(Repo repo, String[] args) {
        String hashCommit = repo.refs().resolve(HEAD);

        while (hashCommit != null && !hashCommit.equals("")) {
            Commit commit = repo.objects().get(Commit.class, hashCommit);
            System.out.println(commit.toString());
            hashCommit = commit.getParent();
        }
    }

    @Override
    public boolean checkOperands(String[] args) {
        return args.length == 0;
    }

    @Override
    public boolean needsRepo() {
        return true;
    }

}
