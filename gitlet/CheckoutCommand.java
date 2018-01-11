/**
 *
 */
package gitlet;

import static gitlet.ReferenceType.BRANCH;
import static gitlet.ReferenceType.HEAD;

/**
 * @author Winston
 */
public class CheckoutCommand implements CommandInterface {

    @Override
    public void runCommand(Repo repo, String[] args) {
        switch (args.length) {
        case 1: checkoutBranch(repo, args[0]);
            break;
        case 2: checkoutFile(repo, repo.refs().resolve(HEAD), args[1]);
            break;
        case 3: checkoutFile(repo, args[0], args[2]);
            break;
        default:
            break;
        }
    }

    /**
     * Checkout a file from a commit.
     * @param repo the repository
     * @param filename the file name
     * @param commitID the commit hash ID
     */
    public static void checkoutFile(Repo repo, String commitID,
                                    String filename) {
        Commit checkCommit;
        switch (commitID.length()) {
        case Utils.UID_LENGTH:
            checkCommit = repo.objects().get(Commit.class, commitID);
            break;
        default:
            checkCommit = repo.objects().find(Commit.class, commitID);
            break;
        }
        if (checkCommit == null) {
            throw new GitletException(
                    "No commit with that id exists.");
        }

        repo.checkout(checkCommit, filename, false);
    }

    /**
     * Checks out an entire branch.
     * @param repo the repository
     * @param branch the branch to which to change
     */
    public static void checkoutBranch(Repo repo, String branch) {
        Reference refBranch = repo.refs().get(BRANCH, branch);
        if (refBranch.equals(repo.getCurrentBranch())) {
            throw new GitletException(
                    "No need to checkout the current branch.");
        }
        String commitHash = refBranch.target();
        repo.checkout(repo.objects().get(Commit.class, commitHash));
        repo.setCurrentBranch(branch);
        repo.getCurrentBranch().setTarget(commitHash);

    }

    @Override
    public boolean checkOperands(String[] args) {
        return args.length == 1 && !args[0].equals("--")
                || args.length == 3 && args[1].equals("--")
                || args.length == 2 && args[0].equals("--");
    }

    @Override
    public boolean needsRepo() {
        return true;
    }

}
