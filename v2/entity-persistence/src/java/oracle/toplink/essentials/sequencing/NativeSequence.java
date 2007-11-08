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

import oracle.toplink.essentials.queryframework.*;
import oracle.toplink.essentials.internal.databaseaccess.DatabasePlatform;
import oracle.toplink.essentials.internal.helper.Helper;
import oracle.toplink.essentials.exceptions.ValidationException;

/**
 * <p>
 * <b>Purpose</b>: Define a database's native sequencing mechanism.
 * <p>
 * <b>Description</b>
 * Many databases have built in support for sequencing.
 * This can be a SEQUENCE object such as in Oracle,
 * or a auto-incrementing column such as the IDENTITY field in Sybase.
 * For an auto-incrementing column the preallocation size is always 1.
 * For a SEQUENCE object the preallocation size must match the SEQUENCE objects "increment by".
 */
public class NativeSequence extends QuerySequence {
    /**
     * true indicates that identity should be used - if the platform supports identity.
     * false indicates that sequence objects should be used - if the platform supports sequence objects.
     */
    protected boolean shouldUseIdentityIfPlatformSupports = true;
    
    public NativeSequence() {
        super();
        setShouldSkipUpdate(true);
    }
    
    public NativeSequence(boolean shouldUseIdentityIfPlatformSupports) {
        super();
        setShouldSkipUpdate(true);
        setShouldUseIdentityIfPlatformSupports(shouldUseIdentityIfPlatformSupports);
    }
    
    /**
     * Create a new sequence with the name.
     */
    public NativeSequence(String name) {
        super(name);
        setShouldSkipUpdate(true);
    }
    
    public NativeSequence(String name, boolean shouldUseIdentityIfPlatformSupports) {
        super(name);
        setShouldSkipUpdate(true);
        setShouldUseIdentityIfPlatformSupports(shouldUseIdentityIfPlatformSupports);
    }
    
    /**
     * Create a new sequence with the name and sequence pre-allocation size.
     */
    public NativeSequence(String name, int size) {
        super(name, size);
        setShouldSkipUpdate(true);
    }

    public NativeSequence(String name, int size, boolean shouldUseIdentityIfPlatformSupports) {
        super(name, size);
        setShouldSkipUpdate(true);
        setShouldUseIdentityIfPlatformSupports(shouldUseIdentityIfPlatformSupports);
    }

    public NativeSequence(String name, int size, int initialValue) {
        super(name, size, initialValue);
        setShouldSkipUpdate(true);
    }    

    public NativeSequence(String name, int size, int initialValue, boolean shouldUseIdentityIfPlatformSupports) {
        super(name, size, initialValue);
        setShouldSkipUpdate(true);
        setShouldUseIdentityIfPlatformSupports(shouldUseIdentityIfPlatformSupports);
    }    

    public void setShouldUseIdentityIfPlatformSupports(boolean shouldUseIdentityIfPlatformSupports) {
        this.shouldUseIdentityIfPlatformSupports = shouldUseIdentityIfPlatformSupports;
    }
    
    public boolean shouldUseIdentityIfPlatformSupports() {
        return shouldUseIdentityIfPlatformSupports;
    }

    public boolean equals(Object obj) {
        if (obj instanceof NativeSequence) {
            return equalNameAndSize(this, (NativeSequence)obj);
        } else {
            return false;
        }
    }

    /**
    * INTERNAL:
    */
    protected ValueReadQuery buildSelectQuery() {
        if(this.shouldAcquireValueAfterInsert()) {
            return ((DatabasePlatform)getDatasourcePlatform()).buildSelectQueryForIdentity();
        } else {
            return ((DatabasePlatform)getDatasourcePlatform()).buildSelectQueryForSequenceObject();
        }
    }

    /**
    * INTERNAL:
    */
    protected ValueReadQuery buildSelectQuery(String seqName, Integer size) {
        if(this.shouldAcquireValueAfterInsert()) {
            return ((DatabasePlatform)getDatasourcePlatform()).buildSelectQueryForIdentity(seqName, size);
        } else {
            return ((DatabasePlatform)getDatasourcePlatform()).buildSelectQueryForSequenceObject(seqName, size);
        }
    }

    /**
    * INTERNAL:
    */
    public void onConnect() {
        DatabasePlatform dbPlatform = null;
        try {
            dbPlatform = (DatabasePlatform)getDatasourcePlatform();
        } catch (ClassCastException ex) {
            if (getSelectQuery() == null) {
                throw ValidationException.platformDoesNotSupportSequence(getName(), Helper.getShortClassName(getDatasourcePlatform()), Helper.getShortClassName(this));
            }
        }
        if (!dbPlatform.supportsNativeSequenceNumbers() && (getSelectQuery() == null)) {
            throw ValidationException.platformDoesNotSupportSequence(getName(), Helper.getShortClassName(getDatasourcePlatform()), Helper.getShortClassName(this));
        }
        // Set shouldAcquireValueAfterInsert flag: identity -> true; sequence objects -> false.
        if(dbPlatform.supportsIdentity() && shouldUseIdentityIfPlatformSupports()) {
            // identity is both supported by platform and desired by the NativeSequence
            setShouldAcquireValueAfterInsert(true);
        } else if(dbPlatform.supportsSequenceObjects() && !shouldUseIdentityIfPlatformSupports()) {
            // sequence objects is both supported by platform and desired by the NativeSequence
            setShouldAcquireValueAfterInsert(false);
        } else {
            if(dbPlatform.supportsNativeSequenceNumbers()) {
                // platform support contradicts to NativeSequence setting - go with platform supported choice.
                // platform must support either identity or sequence objects (otherwise ValidationException would've been thrown earlier),
                // therefore here dbPlatform.supportsIdentity() == !dbPlatform.supportsSequenceObjects().
                setShouldAcquireValueAfterInsert(dbPlatform.supportsIdentity());
            }
        }
        setShouldUseTransaction(dbPlatform.shouldNativeSequenceUseTransaction());
        super.onConnect();
    }

    /**
    * INTERNAL:
    */
    public void onDisconnect() {
        setShouldAcquireValueAfterInsert(false);
        setShouldUseTransaction(false);
        super.onDisconnect();
    }
}
