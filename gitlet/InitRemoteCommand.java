/**
 *
 */
package gitlet;

import static gitlet.ReferenceType.BRANCH;
import static gitlet.ReferenceType.HEAD;
import static gitlet.ReferenceType.TAG;

/**
 * @author Winston
 */
public class InitRemoteCommand implements CommandInterface {

    @Override
    public void runCommand(Repo repo, String[] args) {
        repo.init();
        String initCommit = repo.refs().get(TAG, "iniital").target();
        repo.objects().remove(Commit.class, initCommit);
        repo.refs().get(HEAD).setTarget("");
        repo.refs().remove(BRANCH, "master");

    }

    @Override
    public boolean checkOperands(String[] args) {
        return args.length == 0;
    }

    @Override
    public boolean needsRepo() {
        return false;
    }

}
