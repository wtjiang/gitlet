/**
 *
 */
package gitlet;

import static gitlet.ReferenceType.REMOTE;

/**
 * @author Winston
 */
public class RemoveRemoteCommand implements CommandInterface {

    @Override
    public void runCommand(Repo repo, String[] args) {
        String remote = args[0];
        repo.refs().remove(REMOTE, remote);

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
