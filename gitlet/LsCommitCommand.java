/**
 *
 */
package gitlet;

import static gitlet.ReferenceType.HEAD;

/**
 * @author Winston
 */
public class LsCommitCommand implements CommandInterface {

    @Override
    public void runCommand(Repo repo, String[] args) {
        Commit commit;
        if (args.length == 0) {
            commit = repo.objects().get(Commit.class,
                    repo.refs().resolve(HEAD));
        } else {
            commit = repo.objects().find(Commit.class, args[0]);
        }
        if (commit == null) {
            throw new GitletException("No such commit exists.");
        }

        System.out.println(commit.toString());
        commit.forEach((name, hash) -> {
                System.out.println(name + "\t" + hash);
            });

    }

    @Override
    public boolean checkOperands(String[] args) {
        return args.length == 1 || args.length == 0;
    }

    @Override
    public boolean needsRepo() {
        return true;
    }

}
