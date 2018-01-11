/**
 *
 */
package gitlet;

/**
 * @author Winston
 */
public class GlobalLogCommand implements CommandInterface {

    @Override
    public void runCommand(Repo repo, String[] args) {
        repo.objects().forEach(Commit.class, (hash, com) -> {
                System.out.println(com);
            });
    }

    @Override
    public boolean checkOperands(String[] args) {
        return args.length == 0;
    }

    @Override
    public boolean needsRepo() {
        return true;
    }

}
