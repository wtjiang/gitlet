/**
 *
 */
package gitlet;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * @author Winston
 */
public class AddCommand implements CommandInterface {

    @Override
    public void runCommand(Repo repo, String[] args) {
        String fileName = args[0];
        Path workingDir = repo.getWorkingDir1();
        Path pathTo = workingDir.resolve(fileName);
        File filee = pathTo.toFile();
        if (Files.isDirectory(pathTo)) {
            throw new GitletException("Cannot add a directory.");
        }

        if (!Files.exists(pathTo)) {
            throw new GitletException("File does not exist.");
        }

        Blobs fileBlobs = new Blobs(Utils.readContents(filee));
        String blobHash = repo.objects().put(fileBlobs);
        GitletIndex gitletIndex = repo.index();

        gitletIndex.add(fileName, blobHash);
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
