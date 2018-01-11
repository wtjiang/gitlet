/**
 *
 */
package gitlet;

import static gitlet.ReferenceType.HEAD;

import java.io.IOException;
import java.nio.file.Files;

/**
 * @author Winston
 */
public class RemoveCommand implements CommandInterface {

    @Override
    public void runCommand(Repo repo, String[] args) {
        remove(repo, args[0],
                repo.objects().get(Commit.class, repo.refs().resolve(HEAD)));
    }

    /**
     * remove function.
     * @param repo the repository
     * @param file the file
     * @param head the head
     */
    public static void remove(Repo repo, String file, Commit head) {
        GitletIndex gitletIndex = repo.index();

        if (head.containsKey(file)) {
            try {
                gitletIndex.remove(file, true);
                if (Files.exists(repo.getWorkingDir1().resolve(file))) {
                    Files.delete(repo.getWorkingDir1().resolve(file));
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            gitletIndex.remove(file, false);
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
