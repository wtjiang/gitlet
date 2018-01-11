/**
 *
 */
package gitlet;

import java.util.Map;
import java.util.HashMap;
import java.util.Date;
import java.util.Set;
import java.util.Collection;

import java.util.function.BiConsumer;

/**
 * @author Winston Git commit tree
 */
public class Commit extends GitletSerializer implements Map<String, String> {

    /** the parent. */
    private String parent;

    /** the message. */
    private String message;

    /** The hash map of filename-sha1. */
    private HashMap<String, String> blobs;

    /** the date and time, string representation. */
    private String dateTime;


    /**
     * Creates a commit with a set of blobs.
     * @param messages the commit message.
     * @param datee the date.
     * @param parentt the parent commit.
     * @param blobss the blobs involved in the commit.
     */
    public Commit(String messages, Date datee, String parentt,
            HashMap<String, String> blobss) {
        if (messages == null || messages.isEmpty() || messages.equals("")) {
            throw new GitletException(
                    "Please enter a commit message.");
        }
        this.parent = parentt;
        this.message = messages;
        this.blobs = blobss;
        CommitTime tempTime = new CommitTime(datee);
        this.dateTime = tempTime.getStringTime();
    }

    /**
     * Creates an initial commit.
     * @param messages The initial message.
     * @param initialDate Wed Dec 31 16:00:00 1969 -0800
     */
    public Commit(String messages, Date initialDate) {
        this(messages, initialDate, "", new HashMap<>());
    }

    /**
     * the given branch.
     */
    private String branch;

    /**
     * Creates a merge commit.
     * @param messages the message
     * @param datee the time
     * @param parentt the parent
     * @param branchh the given branch
     * @param blobss the blobs
     */
    public Commit(String messages, Date datee,
                  String parentt, String branchh,
                  HashMap<String, String> blobss) {
        this(messages, datee, parentt, blobss);
        this.branch = branchh;
    }


    /**
     * @return the blobs filename-sha1
     */
    public HashMap<String, String> getBlobs() {
        return this.blobs;
    }

    /**
     * Gets the toString of the commit.
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("===\n");
        sb.append("commit " + this.sha1());
        sb.append("\n");
        if (this.message.contains("Merged")) {
            sb.append("Merge: " + this.branch.subSequence(0, 7)
                   + " " + this.parent.subSequence(0, 7) + " \n");
        }
        sb.append("Date: " + this.getDate());
        sb.append("\n");
        sb.append(this.message);
        sb.append("\n");
        return sb.toString();
    }

    /**
     * @return the parent
     */
    public String getParent() {
        return this.parent;
    }

    /**
     * @return the message
     */
    public String getMessage() {
        return this.message;
    }


    /**
     * Determines the size of the blobs.
     */
    @Override
    public int size() {
        return this.blobs.size();
    }

    /**
     * Determines if a commit is empty.
     */
    @Override
    public boolean isEmpty() {
        return this.blobs.isEmpty();
    }


    /**
     * @return the date
     */
    public String getDate() {
        return this.dateTime;
    }


    /**
     * Gets a blob hash from a file name.
     */
    @Override
    public String get(Object fileName) {
        return this.blobs.get(fileName);
    }

    /**
     * Puts an element in the blob set.
     */
    @Override
    public String put(String fileName, String hash) {
        return this.blobs.put(fileName, hash);
    }

    /**
     * Removes an element from the blob set.
     */
    @Override
    public String remove(Object fileName) {
        return this.blobs.remove(fileName);
    }


    /**
     * Iterates over the blobs.
     * @param action
     *            The action.
     */
    @Override
    public void forEach(BiConsumer<? super String, ? super String> action) {
        this.blobs.forEach(action);
    }

    /**
     * Puts a bunch of blobs in the commit.
     */
    @Override
    public void putAll(Map<? extends String, ? extends String> m) {
        this.blobs.putAll(m);

    }

    /**
     * Clears the blobs.
     */
    @Override
    public void clear() {
        this.blobs.clear();
    }

    /**
     * Determines if the commit references a file.
     */
    @Override
    public boolean containsKey(Object fileName) {
        return this.blobs.containsKey(fileName);
    }

    /**
     * Determines if the commit contains a blob hash.
     */
    @Override
    public boolean containsValue(Object hash) {
        return this.blobs.containsValue(hash);
    }

    /**
     * Gets the blob keyset.
     */
    @Override
    public Set<String> keySet() {
        return this.blobs.keySet();
    }

    /**
     * Gets the blobs values.
     */
    @Override
    public Collection<String> values() {
        return this.blobs.values();
    }

    /**
     * Gets the blobs entry set.
     */
    @Override
    public Set<java.util.Map.Entry<String, String>> entrySet() {
        return this.blobs.entrySet();
    }

}
