/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.openide.util.lookup;

import java.util.Comparator;
import org.openide.util.lookup.AbstractLookup.Pair;


/** Implementation of comparator for AbstractLookup.Pair
 *
 * @author  Jaroslav Tulach
 */
final class ALPairComparator implements Comparator<Pair<?>> {
    public static final Comparator<Pair<?>> DEFAULT = new ALPairComparator();

    /** Creates a new instance of ALPairComparator */
    private ALPairComparator() {
    }

    /** Compares two items.
    */
    public int compare(Pair<?> i1, Pair<?> i2) {
        int result = i1.getIndex() - i2.getIndex();

        if (result == 0) {
            if (i1 != i2) {
                java.io.ByteArrayOutputStream bs = new java.io.ByteArrayOutputStream();
                java.io.PrintStream ps = new java.io.PrintStream(bs);

                ps.println(
                    "Duplicate pair in tree" + // NOI18N
                    "Pair1: " + i1 + " pair2: " + i2 + " index1: " + i1.getIndex() + " index2: " +
                    i2.getIndex() // NOI18N
                     +" item1: " + i1.getInstance() + " item2: " + i2.getInstance() // NOI18N
                     +" id1: " + Integer.toHexString(System.identityHashCode(i1)) // NOI18N
                     +" id2: " + Integer.toHexString(System.identityHashCode(i2)) // NOI18N
                );

                //                print (ps, false);
                ps.close();

                throw new IllegalStateException(bs.toString());
            }

            return 0;
        }

        return result;
    }
}
