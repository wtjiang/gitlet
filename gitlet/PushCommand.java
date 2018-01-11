/**
 *
 */
package gitlet;

import static gitlet.ReferenceType.BRANCH;
import static gitlet.ReferenceType.HEAD;
import static gitlet.ReferenceType.REMOTE;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Winston
 */
public class PushCommand implements CommandInterface {

    @Override
    public void runCommand(final Repo repo, String[] args) {
        push(repo, args[0], args[1]);
    }

    /**
     * Pushes a branch locally to the remote.
     * @param repo
     *            The repo.
     * @param remoteName
     *            The remote path.
     * @param remoteBranch
     *            The remote branch.
     */
    public static void push(final Repo repo, String remoteName,
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

        String head = repo.refs().resolve(HEAD);

        String remoteHead = "";
        if (remote.refs().contains(BRANCH, remoteBranch)) {
            remoteHead = remote.refs().resolve(BRANCH, remoteBranch);
        } else {
            remote.refs().add(BRANCH, remoteBranch, new Reference(""));
        }

        if (head.equals(remoteHead)) {
            return;
        }

        Collection<String> intersecting =
                branchMerge(repo, remote, head, remoteHead);
        pushCommits(repo, remote, intersecting);
        remoteForward(remote, remoteBranch, head);

        remote.close();
    }

    /**
     * Feeds the remote forward.
     * @param remote the remote.
     * @param remoteBranch the remote branch
     * @param head the head
     */
    private static void remoteForward(Repo remote, String remoteBranch,
                                      String head) {
        if (remote.refs().get(HEAD).target().equals(remoteBranch)) {
            ResetCommand.reset(remote, head);
        } else {
            remote.refs().get(BRANCH, remoteBranch).setTarget(head);
        }
    }

    /**
     * Pushes objects in a collection of outgoing commits.
     * @param repo the repository
     * @param remote the remote repository
     * @param outGoingCommits the outgoing commits
     */
    private static void pushCommits(Repo repo, Repo remote,
                                    Collection<String> outGoingCommits) {
        Collection<GitletSerializer> outgoing = new HashSet<>();

        for (String hash : outGoingCommits) {

            Commit c = repo.objects().get(Commit.class, hash);
            List<GitletSerializer> blobs = c.values().stream().map((x) -> {
                    return repo.objects().get(Blobs.class, x);
                }).collect(Collectors.toList());

            outgoing.add(c);
            outgoing.addAll(blobs);
        }
        remote.objects().putAll(outgoing);
    }

    /**
     * Merges two branches.
     * @param repo The repo.
     * @param remote The remote.
     * @param head The head.
     * @param remoteHead The remote head.
     * @return The commits in the intersection.
     */
    private static Collection<String> branchMerge(Repo repo, Repo remote,
                                                  String head,
                                                  String remoteHead) {
        Collection<String> localHistory = MergeCommand.commitHistory(repo, head,
                new LinkedHashSet<>());
        if (!localHistory.contains(remoteHead) && !remoteHead.isEmpty()) {
            remote.close();
            throw new GitletException(
                    "Please pull down remote changes before pushing.");
        }
        localHistory.remove(MergeCommand.commitHistory(remote, remoteHead,
                new LinkedHashSet<>()));

        return localHistory;
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
