package gitlet;

/**
 * @author Winston
 */
public class ResetCommand implements CommandInterface {

    @Override
    public void runCommand(Repo repo, String[] args) {
        reset(repo, args[0]);
    }

    /**
     * Resets the repo to a given commit.
     * @param repo The repo.
     * @param commitHash The commit.
     */
    public static void reset(Repo repo, String commitHash) {
        Commit toCheck;
        if (commitHash.length() == Utils.UID_LENGTH) {
            toCheck = repo.objects().get(Commit.class, commitHash);
        } else {
            toCheck = repo.objects().find(Commit.class, commitHash);
        }

        if (toCheck == null) {
            throw new GitletException(
                    "No commit with that id exists.");
        }

        repo.checkout(toCheck);
        repo.getCurrentBranch().setTarget(toCheck.sha1());
    }

    @Override
    public boolean checkOperands(String[] args) {
        return args.length == 1;
    }


    @Override
    public boolean needsRepo() {
        return true;
    }

}
