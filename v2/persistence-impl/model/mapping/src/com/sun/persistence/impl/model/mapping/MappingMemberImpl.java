/*
 * The contents of this file are subject to the terms 
 * of the Common Development and Distribution License 
 * (the "License").  You may not use this file except 
 * in compliance with the License.
 * 
 * You can obtain a copy of the license at 
 * glassfish/bootstrap/legal/CDDLv1.0.txt or 
 * https://glassfish.dev.java.net/public/CDDLv1.0.html. 
 * See the License for the specific language governing 
 * permissions and limitations under the License.
 * 
 * When distributing Covered Code, include this CDDL 
 * HEADER in each file and include the License file at 
 * glassfish/bootstrap/legal/CDDLv1.0.txt.  If applicable, 
 * add the following below this CDDL HEADER, with the 
 * fields enclosed by brackets "[]" replaced with your 
 * own identifying information: Portions Copyright [yyyy] 
 * [name of copyright owner]
 */

/*
 * MappingMemberImpl.java
 *
 * Created on May 23, 2000, 12:41 AM
 */


package com.sun.persistence.impl.model.mapping;

import com.sun.org.apache.jdo.model.ModelException;
import com.sun.org.apache.jdo.model.ModelVetoException;
import com.sun.persistence.api.model.mapping.MappingClass;
import com.sun.persistence.api.model.mapping.MappingMember;

import java.beans.PropertyVetoException;
import java.text.Collator;

/**
 * @author Michael Bouschen
 * @author Rochelle Raccah
 */
public abstract class MappingMemberImpl extends MappingElementImpl
        implements MappingMember {

    // <editor-fold desc="//===================== constants & variables =======================">

    /**
     * The name of this member.
     */
    private String name;

    /**
     * The class to which this member belongs.
     */
    private MappingClass declaringClass;

    // </editor-fold>

    // <editor-fold desc="//========================= constructors ============================">

    /**
     * Create new MappingMemberImpl with no corresponding name or declaring
     * class.(  This constructor should only be used for cloning and
     * archiving.)
     */
    protected MappingMemberImpl() {
        this(null, null);
    }

    /**
     * Create new MappingMemberImpl with the corresponding name and declaring
     * class.
     * @param name the name of the member
     * @param declaringClass the class to attach to
     */
    protected MappingMemberImpl(String name,
            MappingClass declaringMappingClass) {
        super();
        this.name = name;
        this.declaringClass = declaringMappingClass;
    }

    // </editor-fold>

    // <editor-fold desc="//======================== Object overrides =========================">

    /**
     * Overrides Object's <code>toString</code> method to return the name of
     * this member.
     * @return a string representation of the object
     */
    public String toString() {
        return getName();
    }

    /**
     * Overrides Object's <code>equals</code> method to compare the name and
     * declaring class name of this mapping member. The method returns
     * <code>false</code> if obj does not have the same dynamic type and a 
     * declaring class with the same name as this mapping member.
     * @param obj the reference object with which to compare.
     * @return <code>true</code> if this object is the same as the obj argument;
     *         <code>false</code> otherwise.
     */
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (obj == this) {
            return true;
        }

        // check for the right class and then do the name check by calling compareTo.
        if ((getClass() == obj.getClass()) && (compareTo(obj) == 0)
                && (obj instanceof MappingMember)) {
            MappingClass declaringClass = getDeclaringMappingClass();
            MappingClass objDeclaringClass = ((MappingMember) obj).getDeclaringMappingClass();

            return ((declaringClass == null)
                    ? (objDeclaringClass == null)
                    : declaringClass.equals(objDeclaringClass));
        }

        return false;
    }

    /**
     * Overrides Object's <code>hashCode</code> method to return the hashCode of
     * this member's name plus the hashCode of this mapping member's declaring
     * class.
     * @return a hash code value for this object.
     */
    public int hashCode() {
        MappingClass declaringClass = getDeclaringMappingClass();

        return ((getName() == null) ? 0 : getName().hashCode()
                + ((declaringClass == null) ? 0 : declaringClass.hashCode()));
    }

    // </editor-fold>

    // <editor-fold desc="//================== MappingElementImpl overrides ===================">

    /**
     * Fires property change event.  This method overrides that of
     * MappingElementImpl to update the MappingClassImpl's modified status.
     * @param name property name
     * @param o old value
     * @param n new value
     */
    protected void firePropertyChange(String name, Object o, Object n) {
        // even though o == null and n == null will signify a change, that
        // is consistent with PropertyChangeSupport's behavior and is
        // necessary for this to work
        boolean noChange = ((o != null) && (n != null) && o.equals(n));
        MappingClass classElement = getDeclaringMappingClass();

        super.firePropertyChange(name, o, n);

        if ((classElement != null) && !noChange) {
            classElement.setModified(true);
        }
    }

    /**
     * Fires vetoable change event.  This method overrides that of
     * MappingElementImpl to give listeners a chance to block changes on the
     * mapping class modified status.
     * @param name property name
     * @param o old value
     * @param n new value
     * @throws PropertyVetoException when the change is vetoed by a listener
     */
    protected void fireVetoableChange(String name, Object o, Object n)
            throws PropertyVetoException {
        // even though o == null and n == null will signify a change, that
        // is consistent with PropertyChangeSupport's behavior and is
        // necessary for this to work
        boolean noChange = ((o != null) && (n != null) && o.equals(n));
        MappingClass classElement = getDeclaringMappingClass();

        super.fireVetoableChange(name, o, n);

        if ((classElement != null) && !noChange) {
            ((MappingClassImplDynamic) classElement).fireVetoableChange(
                    PROP_MODIFIED, Boolean.FALSE, Boolean.TRUE);
        }
    }

    // </editor-fold>

    // <editor-fold desc="//====================== Comparable methods =========================">

    /**
     * Compares this object with the specified object for order. Returns a
     * negative integer, zero, or a positive integer as this object is less
     * than, equal to, or greater than the specified object. The specified
     * object must be mapping member, meaning it must be an instance of class
     * MappingMemberImpl or any subclass. If not a ClassCastException is thrown.
     * The order of MappingMemberImpl objects is defined by the order of their
     * names. Mapping members without name are considered to be less than any
     * named mapping member.
     * @param o the Object to be compared.
     * @return a negative integer, zero, or a positive integer as this object is
     *         less than, equal to, or greater than the specified object.
     * @throws ClassCastException - if the specified object is null or is not an
     * instance of MappingMemberImpl
     */
    public int compareTo(Object o) {
        // null is not allowed
        if (o == null) {
            throw new ClassCastException();
        }
        if (o == this) {
            return 0;
        }

        String thisName = getName();
        // the following statement throws a ClassCastException if o is not a 
        // MappingMemberImpl
        String otherName = ((MappingMemberImpl) o).getName();
        // if this does not have a name it should compare less than any named object
        if (thisName == null) {
            return (otherName == null) ? 0 : -1;
        }
        // if this is named and o does not have a name it should compare greater
        if (otherName == null) {
            return 1;
        }
        // now we know that this and o are named mapping members =>
        // use locale-sensitive String comparison
        int ret = Collator.getInstance().compare(thisName, otherName);
        // if both names are equal, both objects might have different types.
        // If so order both objects by their type names (necessary to be consistent with equals)
        if ((ret == 0) && (getClass() != o.getClass())) {
            ret = getClass().getName().compareTo(o.getClass().getName());
        }
        return ret;
    }

    // </editor-fold>

    // <editor-fold desc="//========= MappingMember & related convenience methods =============">

    // <editor-fold desc="//======================= declaring class ===========================">

    /**
     * Get the declaring mapping class.
     * @return the mapping class that owns this member, or <code>null</code> if
     *         the member is not attached to any mapping class
     */
    public MappingClass getDeclaringMappingClass() {
        return declaringClass;
    }

    /**
     * Set the declaring mapping class of this member.  This method should only
     * be used internally and for cloning and archiving or from subclasses 
     * which need to set this based on a derived value in the constructor.
     * @param declaringClass the declaring mapping class of this member
     */
    void setDeclaringMappingClass(MappingClass declaringClass) {
           this.declaringClass = declaringClass;
       }

    // </editor-fold>

    // <editor-fold desc="//======================== name handling ============================">

    /**
     * Get the name of this mapping member.
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * sets the name without triggering property change events
     */
    void setNameInternal (String name) throws ModelException {
        this.name = name;
    }

    /**
     * Set the name of this mapping member.
     * @param name the name
     * @throws ModelException if impossible
     */
    public void setName(String name) throws ModelException {
        String old = getName();

        try {
            fireVetoableChange(PROP_NAME, old, name);
            setNameInternal(name);
            firePropertyChange(PROP_NAME, old, name);
        } catch (PropertyVetoException e) {
            throw new ModelVetoException(e);
        }
    }

    // </editor-fold>

    // </editor-fold> 
}
