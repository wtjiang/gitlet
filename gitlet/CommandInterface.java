/**
 *
 */
package gitlet;

/**
 * @author Winston
 */
public interface CommandInterface {
    /**
     * run function.
     * @param repo the repository
     * @param args the args
     */
    void runCommand(Repo repo, String[] args);

    /**
     * boolean requires repo.
     * @return true or false
     */
    boolean needsRepo();

    /**
     * boolean check operands.
     * @param args the args
     * @return true or false
     */
    boolean checkOperands(String[] args);
}
