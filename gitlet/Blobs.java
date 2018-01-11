package gitlet;
/**
 * @author Winston
 */

public class Blobs extends GitletSerializer {

    /**
     * Generates a blobContents.
     */
    private byte[] blobContents;
    /**
     * string blobContents.
     */
    private String sContents;
    /**
     * Generates a Blobs.
     * @param contents blob array
     */
    public Blobs(byte[] contents) {
        this.blobContents = contents;
        this.sContents = new String(contents);
    }

    /**
     * @return the blobContents
     */
    public byte[] getBlobContents() {
        return this.blobContents;
    }

    /**
     * @return the blobContents
     */
    public String getStringContents() {
        return this.sContents;
    }




}
