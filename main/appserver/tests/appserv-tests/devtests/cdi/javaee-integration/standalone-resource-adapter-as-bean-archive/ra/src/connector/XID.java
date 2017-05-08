/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2010-2017 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://glassfish.dev.java.net/public/CDDL+GPL_1_1.html
 * or packager/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at packager/legal/LICENSE.txt.
 *
 * GPL Classpath Exception:
 * Oracle designates this particular file as subject to the "Classpath"
 * exception as provided by Oracle in the GPL Version 2 section of the License
 * file that accompanied this code.
 *
 * Modifications:
 * If applicable, add the following below the License Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyright [year] [name of copyright owner]"
 *
 * Contributor(s):
 * If you wish your version of this file to be governed by only the CDDL or
 * only the GPL Version 2, indicate your decision by adding "[Contributor]
 * elects to include this software in this distribution under the [CDDL or GPL
 * Version 2] license."  If you don't indicate a single choice of license, a
 * recipient has the option to distribute your version of this file under
 * either the CDDL, the GPL Version 2 or to extend the choice of license to
 * its licensees as provided above.  However, if you add GPL Version 2 code
 * and therefore, elected the GPL Version 2 license, then the option applies
 * only if the new code is made subject to such option by the copyright
 * holder.
 */

package connector;

import javax.transaction.xa.Xid;

/**
 * The XID class provides an implementation of the X/Open
 * transaction identifier it implements the javax.transaction.xa.Xid interface.
 */
public class XID implements Xid {


 private static int ID = initializeID();

    private static int initializeID() {
        return  (int)(Math.random()*100000);
    }

    public int formatID;   // Format identifier
                           // (-1) means that the XID is null
    public int branchQualifier;
    public int globalTxID;

    static public  final int MAXGTRIDSIZE= 64; 
    static public  final int MAXBQUALSIZE= 64; 

    public XID() {
        int foo = ID++;
        formatID = foo;
        branchQualifier = foo;
        globalTxID = foo;
    }

    public boolean equals(Object o) {
        XID               other;   // The "other" XID
        int               L;       // Combined gtrid_length + bqual_length
        int               i;

        if (!(o instanceof XID))        // If the other XID isn't an XID
        {
            return false;                  // It can't be equal
        }
        
        other = (XID)o;                   // The other XID, now properly cast
        
        if (this.formatID == other.formatID 
                && this.branchQualifier == other.branchQualifier
                && this.globalTxID == other.globalTxID) {
            return true;
        }

        return false;
    }

    /**
     * Compute the hash code.
     *
     * @return the computed hashcode
     */
    public int hashCode() {
        if (formatID == (-1)) {
            return (-1);
        }

        return formatID + branchQualifier + globalTxID;

    }

    /*
     * Convert to String
     *
     * <p> This is normally used to display the XID when debugging.
     */

    /**
     * Return a string representing this XID.
     *
     * @return the string representation of this XID
     */
    public String toString() {

        String s = new String("{XID: " +
                "formatID("     + formatID     + "), " +
                "branchQualifier (" + branchQualifier + "), " +
                "globalTxID(" + globalTxID + ")}");
        
        return s;
    }

    /*
     * Return branch qualifier
     */

    /**
     * Returns the branch qualifier for this XID.
     *
     * @return the branch qualifier
     */
    public byte[] getBranchQualifier() {
        String foo = (new Integer(branchQualifier)).toString();
        return foo.getBytes();
    }

    public int getFormatId() {
        return formatID;
    }

    public byte[] getGlobalTransactionId() {
        String foo = (new Integer(globalTxID)).toString();
        return foo.getBytes();
    }
}
