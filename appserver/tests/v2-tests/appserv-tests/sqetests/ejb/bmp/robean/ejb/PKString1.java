/*
 * Copyright 2004-2005 Sun Microsystems, Inc.  All rights reserved.
 * Use is subject to license terms.
 */

package samples.ejb.bmp.robean.ejb;

public class PKString1 implements java.io.Serializable {
    public String pkString1 = null;

    public PKString1() {
    }

    public PKString1(String pkString1) {
        this.pkString1 = pkString1;
    }

    public String getPK() {
        return pkString1;
    }

    public int hashCode() {
        return pkString1.hashCode();
    }

    public boolean equals(Object other) {
        if (other instanceof PKString1) {
            return ((PKString1) other).pkString1.equals(pkString1);
        }
        return false;
    }
}
