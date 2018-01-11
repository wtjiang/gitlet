/**
 *
 */
package gitlet;

import static gitlet.ReferenceType.BRANCH;
import static gitlet.ReferenceType.HEAD;

/**
 * @author Winston
 */
public class RemoveBranchCommand implements CommandInterface {

    @Override
    public void runCommand(Repo repo, String[] args) {
        String branch = args[0];
        if (repo.refs().get(HEAD).target().equals(branch)) {
            throw new GitletException(
                    "Cannot remove the current branch.");
        }

        repo.refs().remove(BRANCH, args[0]);
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
