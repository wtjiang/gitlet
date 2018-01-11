/**
 *
 */
package gitlet;

import static gitlet.ReferenceType.REMOTE;

/**
 * @author Winston
 */
public class AddRemoteCommand implements CommandInterface {

    @Override
    public void runCommand(Repo repo, String[] args) {
        String remote = args[0];
        String targetDirectory = args[1];

        repo.refs().add(REMOTE, remote, new Reference(targetDirectory));
    }

    @Override
    public boolean checkOperands(String[] args) {
        return args.length == 2;
    }

    @Override
    public boolean needsRepo() {
        return true;
    }


}
