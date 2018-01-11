/**
 *
 */
package gitlet;

import java.io.Serializable;

/**
 * @author Winston
 */
public abstract class GitletSerializer implements Serializable {


    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (this.getClass() != obj.getClass()) {
            return false;
        }
        GitletSerializer other = (GitletSerializer) obj;

        return this.sha1().equals(other.sha1());
    }

    /**
     * Generates the SHA-1 for the Gitlet object.
     * @return The SHA-1.
     */
    public String sha1() {
        return Utils.sha1(Utils.serialize(this));
    }

    @Override
    public int hashCode() {
        return this.sha1().hashCode();
    }

}
