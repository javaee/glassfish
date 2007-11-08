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
package oracle.toplink.essentials.sequencing;

import java.util.Vector;
import oracle.toplink.essentials.internal.sessions.AbstractSession;
import oracle.toplink.essentials.internal.databaseaccess.Accessor;

/**
 * <p>
 * <b>Purpose</b>: Reference to the default sequence
 * <p>
 */
public class DefaultSequence extends Sequence {
    protected Sequence defaultSequence;

    public DefaultSequence() {
        super();
    }

    // Created with this constructor,
    // DefaultSequence uses preallocation size of defaultSequence.
    public DefaultSequence(String name) {
        super(name, 0);
    }

    public DefaultSequence(String name, int size) {
        super(name, size);
    }

    public DefaultSequence(String name, int size, int initialValue) {
        super(name, size, initialValue);
    }
    
    public Sequence getDefaultSequence() {
        return getDatasourcePlatform().getDefaultSequence();
    }

    public boolean hasPreallocationSize() {
        return size != 0;
    }

    public int getPreallocationSize() {
        if ((size != 0) || (getDefaultSequence() == null)) {
            return size;
        } else {
            return getDefaultSequence().getPreallocationSize();
        }
    }

    public int getInitialValue() {
        if ((initialValue != 0) || (getDefaultSequence() == null)) {
            return initialValue;
        } else {
            return getDefaultSequence().getInitialValue();
        }
    }

    public boolean equals(Object obj) {
        if (obj instanceof DefaultSequence) {
            return equalNameAndSize(this, (DefaultSequence)obj);
        } else {
            return false;
        }
    }

    /**
    * INTERNAL:
    * Indicates whether sequencing value should be acquired after INSERT.
    * Note that preallocation could be used only in case sequencing values
    * should be acquired before insert (this method returns false).
    * In default implementation, it is true for table sequencing and native
    * sequencing on Oracle platform, false for native sequencing on other platforms.
    */
    public boolean shouldAcquireValueAfterInsert() {
        return getDefaultSequence().shouldAcquireValueAfterInsert();
    }

    /**
    * INTERNAL:
    * Indicates whether TopLink should internally call beginTransaction() before
    * getGeneratedValue/Vector, and commitTransaction after.
    * In default implementation, it is true for table sequencing and
    * false for native sequencing.
    */
    public boolean shouldUseTransaction() {
        return getDefaultSequence().shouldUseTransaction();
    }

    /**
    * INTERNAL:
    * Indicates whether existing attribute value should be overridden.
    * This method is called in case an attribute mapped to PK of sequencing-using
    * descriptor contains non-null value.
    * @param seqName String is sequencing number field name
    * @param existingValue Object is a non-null value of PK-mapped attribute.
    */
    public boolean shouldOverrideExistingValue(String seqName, Object existingValue) {
        return getDefaultSequence().shouldOverrideExistingValue(seqName, existingValue);
    }

    /**
    * INTERNAL:
    * Return the newly-generated sequencing value.
    * Used only in case preallocation is not used (shouldUsePreallocation()==false).
    * Accessor may be non-null only in case shouldUseSeparateConnection()==true.
    * Even in this case accessor could be null - if SequencingControl().shouldUseSeparateConnection()==false;
    * Therefore in case shouldUseSeparateConnection()==true, implementation should handle
    * both cases: use a separate connection if provided (accessor != null), or get by
    * without it (accessor == null).
    * @param accessor Accessor is a separate sequencing accessor (may be null);
    * @param writeSession Session is a Session used for writing (either ClientSession or DatabaseSession);
    * @param seqName String is sequencing number field name
    */
    public Object getGeneratedValue(Accessor accessor, AbstractSession writeSession, String seqName) {
        return getDefaultSequence().getGeneratedValue(accessor, writeSession, seqName);
    }

    /**
    * INTERNAL:
    * Return a Vector of newly-generated sequencing values.
    * Used only in case preallocation is used (shouldUsePreallocation()==true).
    * Accessor may be non-null only in case shouldUseSeparateConnection()==true.
    * Even in this case accessor could be null - if SequencingControl().shouldUseSeparateConnection()==false;
    * Therefore in case shouldUseSeparateConnection()==true, implementation should handle
    * both cases: use a separate connection if provided (accessor != null), or get by
    * without it (accessor == null).
    * @param accessor Accessor is a separate sequencing accessor (may be null);
    * @param writeSession Session is a Session used for writing (either ClientSession or DatabaseSession);
    * @param seqName String is sequencing number field name
    * @param size int number of values to preallocate (output Vector size).
    */
    public Vector getGeneratedVector(Accessor accessor, AbstractSession writeSession, String name, int size) {
        return getDefaultSequence().getGeneratedVector(accessor, writeSession, name, size);
    }

    /**
    * INTERNAL:
    * This method is called when Sequencing object is created.
    * It's a chance to do initialization.
    * @param ownerSession DatabaseSession
    */
    protected void onConnect() {
        // nothing to do
    }

    /**
    * INTERNAL:
    * This method is called when Sequencing object is destroyed..
    * It's a chance to do deinitialization.
    */
    public void onDisconnect() {
        // nothing to do
    }
}
