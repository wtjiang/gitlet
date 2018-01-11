package gitlet;

import static gitlet.ReferenceType.BRANCH;
import static gitlet.ReferenceType.HEAD;
import static gitlet.ReferenceType.TAG;

import java.io.IOException;
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Date;
/**
 * Represents a Gitlet repository.
 * @author Winston
 */
public class Repo extends Serializer<Serializable> {

    /** gitlet directory.*/
    private static final String GITLET_DIR = ".gitlet";
    /** the index string.*/
    private static final String INDEX = "index";
    /** object directory.*/
    private static final String OBJ_DIR = "objects/";
    /** refs directory.*/
    private static final String REFS_DIR = "refs/";

    /**
     * The working path.
     */
    private Path workingDir1;

    /**
     * The gitlet directory.
     */
    private Path gitletDir;

    /**
     * Manages all objects in the repository.
     */
    private GitletSerialManager objectMan;

    /**
     * Manages all of the references.
     */
    private ReferenceManager refMan;

    /**
     * The date at commit.
     */
    private Date now = new Date();

    /**
     * The Unix Epoch.
     */
    private Date epoch = new Date(0);

    /**
     * Declares a repository at the workingDIR.
     * @param workingDir
     *            The working dir.
     */
    public Repo(String workingDir) {
        super(Paths.get(workingDir).resolve(GITLET_DIR));
        this.workingDir1 = this.getDirectory().getParent();
        this.gitletDir = this.getDirectory();

        this.objectMan =
                new GitletSerialManager(this.gitletDir.resolve(OBJ_DIR));
        this.refMan = new ReferenceManager(this.gitletDir.resolve(REFS_DIR));

        if (Files.exists(this.gitletDir)) {
            this.open();
        }

    }

    /**
     * Gets the index.
     * @return The index.
     */
    public GitletIndex index() {
        return this.get(GitletIndex.class, INDEX);
    }

    /**
     * Gets the objects in the repository.
     * @return The manager which holds the objects.
     */
    public GitletSerialManager objects() {
        return this.objectMan;
    }

    /**
     * Gets the reference manager.
     * @return reference manager
     */
    public ReferenceManager refs() {
        return this.refMan;
    }

    /**
     * Initializes a repository if one does not already exist there.
     */
    public void init() {
        if (this.isOpen()) {
            throw new GitletException(
                    "A gitlet version-control system "
                           + "already exists in the current directory.");
        }

        super.open();
        this.objectMan.open();
        this.refMan.open();

        String initialCommit = this.objects()
                .put(new Commit("initial commit", epoch));

        this.refs().add(BRANCH, "master", new Reference(initialCommit));
        this.refs().add(HEAD, new Reference(BRANCH, "master"));
        this.refs().add(TAG, "initial", new Reference(initialCommit));

        this.add(INDEX, new GitletIndex());

    }

    /**
     * Checks out a given commit.
     * @param commit
     *            The commit to checkout.
     */
    public void checkout(Commit commit) {

        GitletIndex gitletIndex = this.index();

        try {
            for (Path entry : Files.newDirectoryStream(this.getWorkingDir1(),
                    x -> !Files.isDirectory(x))) {
                String fileName = entry.getFileName().toString();

                if (commit.containsKey(fileName)
                        && (!gitletIndex.getBlobs().containsKey(fileName)
                                || gitletIndex.getStaged()
                        .containsKey(fileName))) {
                    throw new GitletException("There is an untracked "
                            + "file in the way; delete it or add it first.");
                }
            }

            for (Path entry : Files.newDirectoryStream(this.getWorkingDir1(),
                    x -> !Files.isDirectory(x))) {
                String name = entry.getFileName().toString();

                if (gitletIndex.getBlobs().containsKey(name)) {
                    Files.delete(entry);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        commit.getBlobs().forEach((file, hash) -> {
                Blobs blobs = this.objects().get(Blobs.class, hash);
                Path filePath = this.getWorkingDir1().resolve(file);
                try {
                    Files.write(filePath, blobs.getBlobContents());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });

        gitletIndex.checkout(commit);

    }

    /**
     * Checks out a given file of a commit.
     * @param commit the commit
     * @param filename the filename
     * @param stage boolean stage
     */
    public void checkout(Commit commit, String filename, boolean stage) {
        String blobHash = commit.get(filename);
        if (blobHash == null) {
            throw new GitletException(
                    "File does not exist in that commit.");
        }

        Blobs blobs = this.objects().get(Blobs.class, blobHash);
        Path filePath = this.getWorkingDir1().resolve(filename);
        try {
            Files.write(filePath, blobs.getBlobContents());
        } catch (IOException e) {
            e.printStackTrace();
        }
        GitletIndex gitletIndex = this.index();
        gitletIndex.checkout(filename, blobHash, stage);
    }

    /**
     * Adds a commit to the head.
     * @param message the commit message.
     * @param blobs the blobs
     * @return the sha-1 of the commit.
     */
    public String addCommitAtHead(String message,
            HashMap<String, String> blobs) {

        String headHash = this.refs().resolve(HEAD);
        String commitHash =
                this.objects().put(new Commit(message, now, headHash, blobs));

        this.getCurrentBranch().setTarget(commitHash);
        return commitHash;
    }

    /**
     * Adds a merge commit to the head.
     * @param message the commit message
     * @param branch the given branch
     * @param blobs the blobs
     * @return the sha-1 of the commit.
     */
    public String addCommitAtHeadMerge(String message, String branch,
                                           HashMap<String, String> blobs) {
        String headHash = this.refs().resolve(HEAD);
        String otherHash = this.refs().resolve(BRANCH, branch);

        String mergeHash =
                this.objects().put(
                        new Commit(message, now, headHash, otherHash, blobs));

        this.getCurrentBranch().setTarget(mergeHash);
        return mergeHash;
    }

    /**
     * Gets the head commit.
     * @return The hash for the head commit.
     */
    public Reference getCurrentBranch() {
        Reference head = this.refs().get(HEAD);
        return this.refs().get(BRANCH, head.target());
    }

    /**
     * Sets the current branch in the head.
      * @param branch the current branch
     */
    public void setCurrentBranch(String branch) {
        this.refs().get(BRANCH, branch);
        this.refs().get(HEAD).setTarget(branch);
    }

    /**
     * Opens a repository if the repository failed to open in the first place.
     */
    @Override
    public void open() {
        super.open();
        this.refMan.open();
        this.objectMan.open();

    }

    /**
     * Closes a repository and serializes every loaded object.
     */
    @Override
    public void close() {
        super.close();
        this.refMan.close();
        this.objectMan.close();
    }

    /**
     * Gets the working directory.
      * @return working directory
     */
    public Path getWorkingDir1() {
        return this.workingDir1;
    }

    @Override
    /**
     * @return true or false;
     */
    protected boolean niceSerialization() {
        return false;
    }

}
