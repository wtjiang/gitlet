/**
 *
 */
package gitlet;

import static gitlet.ReferenceType.BRANCH;
import static gitlet.ReferenceType.HEAD;
import static gitlet.ReferenceType.TAG;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

/**
 * @author Winston
 */
public class MergeCommand implements CommandInterface {
    /**
     * given branch.
     */
    private String branch1;

    @Override
    public void runCommand(Repo repo, String[] args) {
        this.branch1 = args[0];
        merge(repo, args[0]);
    }
    /**
     * Merges two commits from different repositories.
     * @param repo the repository
     * @param branch branch to merge into
     */
    public static void merge(Repo repo, String branch) {
        if (!repo.refs().contains(BRANCH, branch)) {
            throw new
                    GitletException("A branch with that name does not exist.");
        }

        String otherHash = repo.refs().resolve(BRANCH, branch);
        String headHash = repo.refs().resolve(HEAD);
        String splitHash = getSplitPoint(repo, headHash, otherHash);

        if (splitHash.isEmpty()) {
            splitHash = repo.refs().resolve(TAG, "initial");
        }

        Commit split = repo.objects().get(Commit.class, splitHash);
        Commit head = repo.objects().get(Commit.class, headHash);
        Commit other = repo.objects().get(Commit.class, otherHash);

        if (repo.index().isChanged()) {
            throw new GitletException("You have uncommitted changes.");
        }

        if (otherHash.equals(headHash)) {
            throw new GitletException(
                    "Cannot merge a branch with itself.");
        }

        if (splitHash.equals(otherHash)) {
            throw new GitletException(
                    "Given branch is an ancestor of the current branch.");
        }

        if (splitHash.equals(headHash)) {
            ResetCommand.reset(repo, otherHash);
            throw new GitletException("Current branch fast-forwarded.");
        }

        boolean conflicts = compareMerge(repo, head, other, split);
        if (conflicts) {
            System.out.println("Encountered a merge conflict.");
        }
        repo.addCommitAtHeadMerge("Merged " + branch
                + " into " + repo.refs().get(HEAD).target()
                + ".", branch, repo.index().blobsFromStage());
    }


    /**
     * Handles the actual checkout of the merge.
     * @param repo
     *            The repository.
     * @param head
     *            The head.
     * @param other
     *            The other.
     * @param split
     *            The split.
     * @return if there was a conflict.
     */
    private static boolean compareMerge(Repo repo, Commit head,
                                        Commit other, Commit split) {

        List<String> toCheckout = new ArrayList<>();
        List<String> toRemove = new ArrayList<>();
        List<String> inConflict = new ArrayList<>();

        other.forEach((file, otherHash) -> {
                String splitHash = split.get(file);
                String headHash = head.get(file);
                if (splitHash == null) {
                    if (headHash == null) {
                        toCheckout.add(file);
                    } else if (!headHash.equals(otherHash)) {
                        inConflict.add(file);
                    }
                } else if (!otherHash.equals(headHash)) {
                    if (headHash == null) {
                        if (!otherHash.equals(splitHash)) {
                            inConflict.add(file);
                        }
                    } else if (headHash.equals(splitHash)) {
                        toCheckout.add(file);
                    } else if (!otherHash.equals(headHash)) {
                        inConflict.add(file);
                    }
                }
            });

        head.forEach((file, headHash) -> {
                String splitHash = split.get(file);
                String otherHash = other.get(file);
                if (splitHash != null && otherHash == null) {
                    if (headHash.equals(splitHash)) {
                        toRemove.add(file);
                    } else {
                        inConflict.add(file);
                    }
                }
            });

        mergeCheckout(repo, other, toCheckout);
        mergeRemove(repo, head, toRemove);
        mergeConflict(repo, head, other, inConflict);

        return !inConflict.isEmpty();
    }

    /**
     * Checks out all files that compareMerge deems mergable.
     * @param repo
     *            The repository.
     * @param other
     *            The commit from which to checkout.
     * @param toCheckout
     *            The files to checkout.
     */
    private static void mergeCheckout(Repo repo, Commit other,
                                      Collection<String> toCheckout) {
        Path workingDir = repo.getWorkingDir1();
        GitletIndex gitletIndex = repo.index();

        for (String file : toCheckout) {
            if (Files.exists(workingDir.resolve(file))
                    && !gitletIndex.getBlobs().containsKey(file)) {
                throw new GitletException("There is an untracked "
                        + "file in the way; delete it or add it first.");
            }
        }

        toCheckout.forEach(x -> repo.checkout(other, x, true));
    }

    /**
     * Removes all files from the repository which the compareMerge deems
     * removable.
     * @param repo the repository
     * @param head the head
     * @param toRemove collection to remove
     */
    private static void mergeRemove(Repo repo, Commit head,
                                    Collection<String> toRemove) {
        toRemove.forEach(x -> RemoveCommand.remove(repo, x, head));
    }



    /**
     * Gets the split point.
     * @param repo1 The repository.
     * @param commit1 The first commit.
     * @param commit2 The second commit.
     * @return The splitpoint commit.
     */
    public static String getSplitPoint(
            Repo repo1, String commit1, String commit2) {
        if (commit2.equals(commit1)) {
            return commit1;
        }

        List<String> history1 =
                (List<String>) commitHistory(repo1, commit1, new ArrayList<>());
        Collection<String> history2 =
                commitHistory(repo1, commit2, new HashSet<>());

        history1.retainAll(history2);
        if (history1.isEmpty()) {
            return "";
        } else {
            return history1.get(0);
        }
    }

    /**
     * Gets commit history from a starting point.
     * @param repo the repo
     * @param start the starting point
     * @param history the history
     * @return
     */
    public static Collection<String> commitHistory(Repo repo, String start,
                                                   Collection<String> history) {
        String start1 = start;
        while (!start1.isEmpty()) {
            history.add(start1);
            start1 = repo.objects().get(Commit.class, start1).getParent();
        }
        return history;
    }

    /**
     * Merges the conflicts by displaying their differences.
     * @param repo the repository.
     * @param head The head.
     * @param other The other.
     * @param inConflict The files in conflict.
     */
    private static void mergeConflict(Repo repo, Commit head,
                                      Commit other,
                                      Collection<String> inConflict) {
        GitletIndex gitletIndex = repo.index();
        for (String file : inConflict) {
            Path path = repo.getWorkingDir1().resolve(file);
            File filee = path.toFile();
            byte[] currentContents = new byte[0];
            byte[] mergeContents = new byte[0];

            if (head.containsKey(file)) {
                Blobs headVersion = repo.objects().get(Blobs.class,
                        head.getBlobs().get(file));
                currentContents = headVersion.getBlobContents();
            }

            if (other.getBlobs().containsKey(file)) {
                Blobs otherVersion = repo.objects().get(Blobs.class,
                        other.getBlobs().get(file));
                mergeContents = otherVersion.getBlobContents();
            }

            Utils.writeContents(filee, "<<<<<<< HEAD\n", currentContents,
                    "=======\n", mergeContents, ">>>>>>>\n");

            Blobs entryBlobs = new Blobs(Utils.readContents(filee));
            gitletIndex.add(file, entryBlobs.sha1());

            if (gitletIndex.getBlobs().containsKey(file)) {
                gitletIndex.unstage(file);
            }
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
