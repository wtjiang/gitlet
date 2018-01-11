/**
 *
 */
package gitlet;

import java.io.Serializable;
import java.util.HashMap;
import java.util.TreeMap;

/**
 * @author Winston
 */
public class GitletIndex implements Serializable {

    /**
     * Gets a list of blobs.
     */
    private HashMap<String, String> blobs;

    /**
     * A tree map of the staged files in the index.
     */
    private TreeMap<String, String> staged;

    /**
     * A tree map of the removed (staged) files in the index.
     */
    private TreeMap<String, String> removed;

    /**
     * Creates a gitlet index.
     */
    public GitletIndex() {
        this.blobs = new HashMap<>();
        this.staged = new TreeMap<>();
        this.removed = new TreeMap<>();
    }

    /**
     * Gets the blob from the stage and clears the staging area.
     * @return A hashmap of filenames to blobs.
     */
    public HashMap<String, String> blobsFromStage() {
        if (!this.isChanged()) {
            throw new GitletException("No changes added to the commit.");
        }

        this.clearStage();
        return this.blobs;
    }

    /**
     * Accessor for the blobs.
     * @return blobs
     */
    public HashMap<String, String> getBlobs() {
        return this.blobs;
    }

    /**
     * Checks out a particular file.
     * @param filename the file to checkout.
     * @param hash the hash of the file.
     * @param stage stage boolean
     */
    public void checkout(String filename, String hash, boolean stage) {

        if (stage) {
            this.add(filename, hash);
        } else {
            this.blobs.put(filename, hash);
            this.staged.remove(filename);
            this.removed.remove(filename);
        }
    }

    /**
     * checkout function.
     * @param commit commit
     */
    public void checkout(Commit commit) {
        this.clearStage();
        this.blobs = commit.getBlobs();
    }

    /**
     * Adds a blob to the staging area.
     * @param fileName the file name of the blob.
     * @param hash the hash of the blob.
     */
    public void add(String fileName, String hash) {
        if (this.removed.containsKey(fileName)) {
            String removedHash = this.removed.remove(fileName);
            this.blobs.put(fileName, removedHash);
        } else {
            if (!this.blobs.containsKey(fileName)
                    || !this.blobs.get(fileName).equals(hash)) {
                this.staged.put(fileName, hash);
            }
            this.blobs.put(fileName, hash);
        }

    }

    /**
     * Untracks a file.
     * @param fileName filename
     * @param fromLastCommit boolean
     */
    public void remove(String fileName, boolean fromLastCommit) {
        if (!this.blobs.containsKey(fileName)
                && !this.staged.containsKey(fileName)) {
            throw new GitletException("No reason to remove the file.");
        }
        if (fromLastCommit) {
            this.removed.put(fileName, this.blobs.get(fileName));
        }

        this.staged.remove(fileName);
        this.blobs.remove(fileName);
    }

    /**
     * @return the removed
     */
    public TreeMap<String, String> getRemoved() {
        return this.removed;
    }

    /**
     * @return the union of removed and modified.
     */
    public TreeMap<String, String> getStaged() {
        return this.staged;
    }

    /**
     * unstage function.
     * @param fileName filename
     */
    public void unstage(String fileName) {
        if (!this.blobs.containsKey(fileName)
                && !this.staged.containsKey(fileName)) {
            throw new GitletException("No reason to remove the file.");
        }

        this.staged.remove(fileName);
    }

    /**
     * Clears the stage.
     */
    private void clearStage() {
        this.removed.clear();
        this.staged.clear();
    }


    /**
     * Determines if the staging area has changed.
     * @return If it has changed.
     */
    public boolean isChanged() {
        return this.removed.size() + this.staged.size() != 0;
    }

}
