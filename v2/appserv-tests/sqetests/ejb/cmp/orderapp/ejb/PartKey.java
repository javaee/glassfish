/*
 * Copyright © 2003 Sun Microsystems, Inc.  All rights reserved.  U.S.
 * Government Rights - Commercial software.  Government users are subject
 * to the Sun Microsystems, Inc. standard license agreement and
 * applicable provisions of the FAR and its supplements.  Use is subject
 * to license terms.
 *
 * This distribution may include materials developed by third parties.
 * Sun, Sun Microsystems, the Sun logo, Java and J2EE are trademarks
 * or registered trademarks of Sun Microsystems, Inc. in the U.S. and
 * other countries.
 
 */


package dataregistry;


public final class PartKey implements java.io.Serializable {

    public String partNumber;
    public int revision;

    /**
     * @see Object#equals(Object)
     */
    public boolean equals(Object otherOb) {

        if (this == otherOb) {
            return true;
        }
        if (!(otherOb instanceof PartKey)) {
            return false;
        }
        PartKey other = (PartKey) otherOb;
        return (

        (partNumber==null?other.partNumber==null:partNumber.equals(other.partNumber))
        &&
        (revision == other.revision)

        );
    }

    /**
     * @see Object#hashCode()
     */
    public int hashCode() {
        return (

        (partNumber==null?0:partNumber.hashCode())
        ^
        ((int) revision)

        );
    }

    /**
     * @see Object#toString()
     */
    public String toString() {
        return partNumber + " rev" + revision;
    }

}
