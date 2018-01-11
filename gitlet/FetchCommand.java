/**
 *
 */
package gitlet;

import static gitlet.ReferenceType.BRANCH;
import static gitlet.ReferenceType.REMOTE;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Winston
 */
public class FetchCommand implements CommandInterface {

    @Override
    public void runCommand(Repo repo, String[] args) {
        fetch(repo, args[0], args[1]);
    }

    /**
     * Fetches a remote repository.
     * @param repo Tj.
     * @param remoteName rj
     * @param remoteBranch rb
     */
    public static void fetch(Repo repo, String remoteName,
                             String remoteBranch) {
        Reference remoteRef = repo.refs().get(REMOTE, remoteName);

        Path remoteDir =
                repo.getWorkingDir1().resolve(remoteRef.target()).normalize();

        if (!Files.exists(remoteDir)) {
            throw new GitletException("Remote directory not found.");
        }

        Repo remote =
                new Repo(remoteDir.resolve("..").normalize().toString());
        if (!remote.isOpen()) {
            throw new GitletException("Remote directory not found.");
        }

        if (!remote.refs().contains(BRANCH, remoteBranch)) {
            throw new GitletException("That remote does not have that branch.");
        }

        String remoteHead = remote.refs().resolve(BRANCH, remoteBranch);

        pullObjects(repo, remote, remoteHead);
        String localBranch = remoteName + "/" + remoteBranch;
        if (!repo.refs().contains(BRANCH, localBranch)) {
            repo.refs().add(BRANCH, localBranch, new Reference(remoteHead));
        } else {
            repo.refs().get(BRANCH, localBranch).setTarget(remoteHead);
        }

    }

    /**
     * Pulls objects.
     * @param repo a
     * @param remote  b
     * @param remoteHead c
     */
    private static void pullObjects(Repo repo, Repo remote,
                                    String remoteHead) {
        Collection<GitletSerializer> incoming = new HashSet<>();

        for (String hash : MergeCommand.commitHistory(remote, remoteHead,
                new ArrayList<String>())) {

            Commit c = remote.objects().get(Commit.class, hash);
            List<GitletSerializer> blobs = c.values().stream()
                    .filter(x -> !repo.objects().contains(Blobs.class, x))
                    .map((x) -> {
                            return remote.objects().get(Blobs.class, x);
                        }).collect(Collectors.toList());
            if (!repo.objects().contains(Commit.class, hash)) {
                incoming.add(c);
            }

            incoming.addAll(blobs);
        }
        repo.objects().putAll(incoming);

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
