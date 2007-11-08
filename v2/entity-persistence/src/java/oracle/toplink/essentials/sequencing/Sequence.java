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
import java.io.Serializable;
import oracle.toplink.essentials.internal.sessions.AbstractSession;
import oracle.toplink.essentials.internal.databaseaccess.Platform;
import oracle.toplink.essentials.internal.databaseaccess.DatasourcePlatform;
import oracle.toplink.essentials.internal.databaseaccess.Accessor;
import oracle.toplink.essentials.exceptions.ValidationException;

/**
 * <p>
 * <b>Purpose</b>: Define an interface for sequencing customization.
 * <p>
 * <b>Description</b>: Customary sequencing behavior could be achieved by
 * implementing this interface and passing the instance to
 * DatabaseSession.getSequencingControl().setValueGenerationPolicy(..).
 * TopLink internally uses the same method to set its default implementation
 * of this interface, which provides native sequencing and table sequencing.
 * Note that the following methods:
 *      shouldAcquireValueAfterInsert();
 *      shouldUsePreallocation();
 *      shouldUseSeparateConnection();
 *      shouldUseTransaction();
 * are called only once - during creation of the sequencing object.
 * Therefore during the lifetime of sequencing object these methods
 * should return the same values as when called for the first time.
 * If this is not true - resetSequencing (call SequencingControl.resetSequencing()).
 * <p>
 * <b>Responsibilities</b>:
 * <ul>
 * <li> Define the APIs for customary sequencing.
 * </ul>
 * @see SequencingControl
 */
public abstract class Sequence implements Serializable, Cloneable {
    // name
    protected String name = "";

    // preallocation size
    protected int size = 50;

    // owner platform
    protected Platform platform;
    
    protected int initialValue = 1;

    // number of times onConnect was called - number of times onDisconnect was called
    protected int depth;

    public Sequence() {
        super();
    }

    public Sequence(String name) {
        this();
        setName(name);
    }

    public Sequence(String name, int size) {
        this();
        setName(name);
        setPreallocationSize(size);
    }
    
    public Sequence(String name, int size, int initialValue) {
        this();
        setName(name);
        setPreallocationSize(size);
        setInitialValue(initialValue);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getPreallocationSize() {
        return size;
    }

    public void setPreallocationSize(int size) {
        this.size = size;
    }
    
    public int getInitialValue() {
        return initialValue;
    }

    public void setInitialValue(int initialValue) {
        this.initialValue = initialValue;
    }    

    public Object clone() {
        try {
            Sequence clone = (Sequence)super.clone();
            if (isConnected()) {
                clone.depth = 1;
                clone.onDisconnect(getDatasourcePlatform());
            }
            return clone;
        } catch (Exception exception) {
            throw new InternalError("Clone failed");
        }
    }

    public boolean equals(Object obj) {
        if (obj instanceof Sequence) {
            return equalNameAndSize(this, (Sequence)obj);
        } else {
            return false;
        }
    }

    public static boolean equalNameAndSize(Sequence seq1, Sequence seq2) {
        if (seq1 == seq2) {
            return true;
        }
        return seq1.getName().equals(seq2.getName()) && (seq1.getPreallocationSize() == seq2.getPreallocationSize());
    }

    protected void setDatasourcePlatform(Platform platform) {
        this.platform = platform;
    }

    public Platform getDatasourcePlatform() {
        return platform;
    }

    /**
    * INTERNAL:
    * Indicates whether sequencing value should be acquired after INSERT.
    * Note that preallocation could be used only in case sequencing values
    * should be acquired before insert (this method returns false).
    * In default implementation, it is true for table sequencing and native
    * sequencing on Oracle platform, false for native sequencing on other platforms.
    */
    public abstract boolean shouldAcquireValueAfterInsert();

    /**
    * INTERNAL:
    * Indicates whether several sequencing values should be acquired at a time
    * and be kept by TopLink. This in only possible in case sequencing numbers should
    * be acquired before insert (shouldAcquireValueAfterInsert()==false).
    * In default implementation, it is true for table sequencing and native
    * sequencing on Oracle platform, false for native sequencing on other platforms.
    */
    public boolean shouldUsePreallocation() {
        return !shouldAcquireValueAfterInsert();
    }

    /**
    * INTERNAL:
    * Indicates whether TopLink should internally call beginTransaction() before
    * getGeneratedValue/Vector, and commitTransaction after.
    * In default implementation, it is true for table sequencing and
    * false for native sequencing.
    */
    public abstract boolean shouldUseTransaction();

    /**
    * INTERNAL:
    * Indicates whether existing attribute value should be overridden.
    * This method is called in case an attribute mapped to PK of sequencing-using
    * descriptor contains non-null value.
    * @param seqName String is sequencing number field name
    * @param existingValue Object is a non-null value of PK-mapped attribute.
    */
    public abstract boolean shouldOverrideExistingValue(String seqName, Object existingValue);

    /**
    * INTERNAL:
    * Indicates whether existing attribute value should be overridden.
    * This method is called in case an attribute mapped to PK of sequencing-using
    * descriptor contains non-null value.
    * @param existingValue Object is a non-null value of PK-mapped attribute.
    */
    public boolean shouldOverrideExistingValue(Object existingValue) {
        return shouldOverrideExistingValue(getName(), existingValue);
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
    public abstract Object getGeneratedValue(Accessor accessor, AbstractSession writeSession, String seqName);

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
    */
    public Object getGeneratedValue(Accessor accessor, AbstractSession writeSession) {
        return getGeneratedValue(accessor, writeSession, getName());
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
    public abstract Vector getGeneratedVector(Accessor accessor, AbstractSession writeSession, String seqName, int size);

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
    */
    public Vector getGeneratedVector(Accessor accessor, AbstractSession writeSession) {
        return getGeneratedVector(accessor, writeSession, getName(), getPreallocationSize());
    }

    /**
    * INTERNAL:
    * This method is called when Sequencing object is created.
    * Don't override this method.
    * @param ownerSession DatabaseSession
    */
    public void onConnect(Platform platform) {
        if (isConnected()) {
            verifyPlatform(platform);
        } else {
            setDatasourcePlatform(platform);
            onConnect();
        }
        depth++;
    }

    /**
    * INTERNAL:
    * This method is called when Sequencing object is created.
    * If it requires initialization, subclass should override this method.
    * @param ownerSession DatabaseSession
    */
    protected abstract void onConnect();

    /**
    * INTERNAL:
    * This method is called when Sequencing object is destroyed.
    * Don't overridethis method.
    */
    public void onDisconnect(Platform platform) {
        if (isConnected()) {
            depth--;
            if (depth == 0) {
                onDisconnect();
                setDatasourcePlatform(null);
            }
        }
    }

    /**
    * INTERNAL:
    * This method is called when Sequencing object is destroyed.
    * If it requires deinitialization, subclass should override this method.
    */
    protected abstract void onDisconnect();

    /**
    * PUBLIC:
    * Indicates that Sequence is connected.
    */
    public boolean isConnected() {
        return platform != null;
    }

    /**
    * INTERNAL:
    * Make sure that the sequence is not used by more than one platform.
    */
    protected void verifyPlatform(Platform otherPlatform) {
        if (getDatasourcePlatform() != otherPlatform) {
            String hashCode1 = Integer.toString(System.identityHashCode(getDatasourcePlatform()));
            String name1 = ((DatasourcePlatform)getDatasourcePlatform()).toString() + '(' + hashCode1 + ')';

            String hashCode2 = Integer.toString(System.identityHashCode(otherPlatform));
            String name2 = ((DatasourcePlatform)otherPlatform).toString() + '(' + hashCode2 + ')';

            throw ValidationException.sequenceCannotBeConnectedToTwoPlatforms(getName(), name1, name2);
        }
    }
}
