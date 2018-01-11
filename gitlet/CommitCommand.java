package gitlet;

import java.util.HashMap;

/**
 * @author Winston
 */
public class CommitCommand implements CommandInterface {

    @Override
    public void runCommand(Repo repo, String[] args) {
        String message = args[0];

        GitletIndex gitletIndex = repo.index();
        HashMap<String, String> blobs = gitletIndex.blobsFromStage();

        repo.addCommitAtHead(message, blobs);
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
