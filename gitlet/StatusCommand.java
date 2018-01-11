/**
 *
 */
package gitlet;

import static gitlet.ReferenceType.BRANCH;
import static gitlet.ReferenceType.HEAD;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * @author Winston
 */
public class StatusCommand implements CommandInterface {

    @Override
    public void runCommand(Repo repo, String[] args) {
        String currentBranch = repo.refs().get(HEAD).target();
        System.out.println("=== Branches ===");

        repo.refs().forEach(BRANCH, (name, branch) -> {
                if (name.equals(currentBranch)) {
                    System.out.print('*');
                }
                System.out.println(name);
            });

        GitletIndex gitletIndex = repo.index();
        Path workingDir = repo.getWorkingDir1();

        System.out.println("\n=== Staged Files ===");
        gitletIndex.getStaged().forEach((name, hash)
                -> System.out.println(name));

        System.out.println("\n=== Removed Files ===");
        gitletIndex.getRemoved().forEach((name, hash)
                -> System.out.println(name));

        try {
            this.changesBetween(gitletIndex, workingDir);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    /**
     * Outputs the changes between between the gitletIndex and the working dir.
     * @param gitletIndex gitletIndex.
     * @param workDir working directory.
     */
    private void changesBetween(GitletIndex gitletIndex,
                                Path workDir) throws IOException {
        HashMap<String, String> stagedBlobs = new HashMap<>();
        List<String> notStaged = new ArrayList<>();
        List<String> notTracked = new ArrayList<>();

        for (Path entry : Files.newDirectoryStream(workDir)) {
            if (!Files.isDirectory(entry)) {
                String name = entry.getFileName().toString();
                Blobs entryBlobs = new Blobs(Files.readAllBytes(entry));

                stagedBlobs.put(name, entryBlobs.sha1());
            }
        }

        stagedBlobs.forEach((name, hash) -> {
                if (!gitletIndex.getBlobs().containsKey(name)) {
                    notTracked.add(name);
                } else if (!gitletIndex.getBlobs().get(name).equals(hash)) {
                    notStaged.add(name + " (modified)");
                }
            });

        gitletIndex.getBlobs().forEach((name, hash) -> {
                if (!stagedBlobs.containsKey(name)) {
                    notStaged.add(name + " (deleted)");
                }
            });

        notStaged.sort(String::compareTo);
        System.out.println("\n=== Modifications Not Staged For Commit ===");
        notStaged.forEach(x -> System.out.println(x));
        notTracked.sort(String::compareTo);

        System.out.println("\n=== Untracked Files ===");
        notTracked.forEach(x -> System.out.println(x));
        System.out.println("");
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
