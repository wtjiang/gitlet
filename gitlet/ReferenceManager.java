/**
 *
 */
package gitlet;

import java.nio.file.Path;
import java.util.function.BiConsumer;

/**
 * @author Winston
 */
public class ReferenceManager extends Serializer<Reference> {
    /**
     * constructor for reference manager.
     * @param base the base path
     */
    public ReferenceManager(Path base) {
        super(base);
    }

    /**
     * add reference.
     * @param type the type
     * @param fileName the name
     * @param ref the reference
     * @return the reference
     */
    public Reference add(ReferenceType type, String fileName, Reference ref) {
        try {
            this.add(type.getBaseDir() + fileName, ref);
        } catch (GitletException e) {
            throw new GitletException(
                    "A " + type.toString().toLowerCase()
                            + " with that name already exists.");
        }
        return ref;
    }

    /**
     * add reference.
     * @param type the type
     * @param ref the reference
     * @return the reference
     */
    public Reference add(ReferenceType type, Reference ref) {
        return this.add(type, type.toString(), ref);
    }

    /**
     * get reference.
     * @param type the type
     * @param fileName the name
     * @return the reference
     */
    public Reference get(ReferenceType type, String fileName) {
        Reference ref =
                this.get(Reference.class, type.getBaseDir() + fileName);

        if (ref == null) {
            throw new GitletException(
                    "No such " + type.toString().toLowerCase() + " exists.");
        }
        return ref;
    }

    /**
     * get reference.
     * @param type the type
     * @return the reference
     */
    public Reference get(ReferenceType type) {
        return this.get(type, type.toString());
    }

    /**
     * Determines if the reference manager contains a given file.
     * @param branch the branch
     * @param name the name
     * @return true or false
     */
    public boolean contains(ReferenceType branch, String name) {
        return this.contains(Reference.class, branch.getBaseDir() + name);
    }

    /**
     * resolves.
     * @param type the type
     * @param fileName the name
     * @return the string
     */
    public String resolve(ReferenceType type, String fileName) {
        Reference cur = this.get(type, fileName);
        while (cur.targetIsReference()) {
            cur = this.get(cur.targetType(), cur.target());
        }

        return cur.target();

    }

    /**
     * resolve string.
     * @param type the type
     * @return the string
     */
    public String resolve(ReferenceType type) {
        return this.resolve(type, type.toString());
    }

    /**
     * remove reference.
     * @param type the type
     * @param fileName the name
     */
    public void remove(ReferenceType type, String fileName) {
        try {
            this.remove(Reference.class, type.getBaseDir() + fileName);
        } catch (GitletException e) {
            throw new GitletException(
                    "A " + type.toString().toLowerCase()
                            + " with that name does not exist.");
        }
    }

    /**
     * Iterates over the references of a certain type within the manager.
     * @param type the type of reference.
     * @param action the action.
     */
    public void forEach(ReferenceType type,
            BiConsumer<? super String, Reference> action) {
        this.forEach(Reference.class, (file, ref) -> {
                if (file.startsWith(type.getBaseDir())) {
                    action.accept(file.replace(type.getBaseDir(), ""), ref);
                }
            });
    }

    /**
     * serialization.
     * @return true or false
     */
    @Override
    protected boolean niceSerialization() {
        return false;
    }

}
