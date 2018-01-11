/**
 *
 */
package gitlet;

/**
 * @author Winston
 */
public class PullCommand implements CommandInterface {

    @Override
    public void runCommand(Repo repo, String[] args) {
        FetchCommand.fetch(repo, args[0], args[1]);
        MergeCommand.merge(repo, args[0] + "/" + args[1]);
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
