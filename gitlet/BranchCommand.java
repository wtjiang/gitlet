/**
 *
 */
package gitlet;

import static gitlet.ReferenceType.BRANCH;
import static gitlet.ReferenceType.HEAD;

/**
 * @author Winston
 */
public class BranchCommand implements CommandInterface {

    @Override
    public void runCommand(Repo repo, String[] args) {
        String headCommit = repo.refs().resolve(HEAD);
        repo.refs().add(BRANCH, args[0], new Reference(headCommit));
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
