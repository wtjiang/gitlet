/**
 *
 */
package gitlet;

/**
 * @author Winston
 */
public class FindCommand implements CommandInterface {

    @Override
    public void runCommand(final Repo repo, String[] args) {
        final int[] runIter = new int[] { 0 };
        repo.objects().forEach(Commit.class, (hash, com) -> {
                if (com.getMessage().equals(args[0])) {
                    runIter[0]++;
                    System.out.println(hash);
                }
            });
        if (runIter[0] == 0) {
            throw new GitletException(
                    "Found no commit with that message.");
        }

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
