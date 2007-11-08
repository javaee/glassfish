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


public final class LineItemKey implements java.io.Serializable {

    public Integer orderId;
    public int itemId;

    /**
     * @see Object#equals(Object)
     */
    public boolean equals(Object otherOb) {

        if (this == otherOb) {
            return true;
        }
        if (!(otherOb instanceof LineItemKey)) {
            return false;
        }
        LineItemKey other = (LineItemKey) otherOb;
        return (

        (orderId==null?other.orderId==null:orderId.equals(other.orderId))
        &&
        (itemId == other.itemId)

        );
    }

    /**
     * @see Object#hashCode()
     */
    public int hashCode() {
        return (

        (orderId==null?0:orderId.hashCode())
        ^
        ((int) itemId)

        );
    }

    /**
     * @see Object#toString()
     */
    public String toString() {
        return "" + orderId + "-" + itemId;
    }

}
