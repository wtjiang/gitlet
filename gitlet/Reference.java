/**
 *
 */
package gitlet;

import java.io.Serializable;

/**
 * @author Winston
 */
public class Reference implements Serializable {

    /**
     * The target of the reference.
     */
    private String target;

    /**
     * If the reference links forward.
     */
    private ReferenceType targetType;

    /**
     * reference.
     * @param targetTypee target type
     * @param targetRef target reference
     */
    public Reference(ReferenceType targetTypee, String targetRef) {
        this.targetType = targetTypee;
        this.target = targetRef;
    }

    /**
     * Constructs a reference with a target.
     * @param targett The target.
     */
    public Reference(String targett) {
        this(ReferenceType.NONE, targett);
    }

    /**
     * Gets the target of this reference.
     * @return The target.
     */
    public String target() {
        return this.target;
    }

    /**
     * Sets the target of a reference.
     * @param targett
     *            The reference target.
     */
    public void setTarget(String targett) {
        this.target = targett;
    }

    /**
     * If the target is another reference.
     * @return boolean
     */
    public boolean targetIsReference() {
        return this.targetType != ReferenceType.NONE;
    }

    /**
     * Gets the target type.
      * @return type
     */
    public ReferenceType targetType() {
        return this.targetType;
    }
}
