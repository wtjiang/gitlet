/**
 *
 */
package gitlet;

/**
 * @author Winston
 */
public class InitCommand implements CommandInterface {

    @Override
    public void runCommand(Repo repo, String[] args) {
        repo.init();
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
