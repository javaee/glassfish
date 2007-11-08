/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * // Copyright (c) 1998, 2007, Oracle. All rights reserved.
 * 
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License. You can obtain
 * a copy of the License at https://glassfish.dev.java.net/public/CDDL+GPL.html
 * or glassfish/bootstrap/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 * 
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at glassfish/bootstrap/legal/LICENSE.txt.
 * Sun designates this particular file as subject to the "Classpath" exception
 * as provided by Sun in the GPL Version 2 section of the License file that
 * accompanied this code.  If applicable, add the following below the License
 * Header, with the fields enclosed by brackets [] replaced by your own
 * identifying information: "Portions Copyrighted [year]
 * [name of copyright owner]"
 * 
 * Contributor(s):
 * 
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
package oracle.toplink.essentials.internal.sequencing;

import java.util.Vector;
import java.util.Hashtable;

class PreallocationHandler implements SequencingLogInOut {
    protected Hashtable preallocatedSequences;

    public PreallocationHandler() {
        super();
    }

    /**
    * PROTECTED:
    * return a vector from the global sequences based on
    * seqName.  If there is not one, put it there.
    */
    protected Vector getPreallocatedSequences(String seqName) {
        Vector sequencesForName;
        synchronized (preallocatedSequences) {
            sequencesForName = (Vector)preallocatedSequences.get(seqName);
            if (sequencesForName == null) {
                sequencesForName = new Vector();
                preallocatedSequences.put(seqName, sequencesForName);
            }
        }
        return sequencesForName;
    }

    // SequencingLogInOut
    public void onConnect() {
        initializePreallocated();
    }

    public void onDisconnect() {
        preallocatedSequences = null;
    }

    public boolean isConnected() {
        return preallocatedSequences != null;
    }

    // removes all preallocated objects.
    // a dangerous method to use in multithreaded environment method,
    // but so handy for testing
    public void initializePreallocated() {
        preallocatedSequences = new Hashtable(20);
    }

    // removes all preallocated objects for the specified seqName.
    // a dangerous method to use in multithreaded environment method,
    // but so handy for testing
    public void initializePreallocated(String seqName) {
        preallocatedSequences.remove(seqName);
    }

    public Vector getPreallocated(String seqName) {
        return getPreallocatedSequences(seqName);
    }

    public void setPreallocated(String seqName, Vector sequences) {
        getPreallocatedSequences(seqName).addAll(sequences);
    }
}
