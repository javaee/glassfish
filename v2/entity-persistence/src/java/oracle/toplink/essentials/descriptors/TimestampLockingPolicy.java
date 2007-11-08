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
package oracle.toplink.essentials.descriptors;

import oracle.toplink.essentials.internal.sessions.*;
import java.util.*;
import java.sql.Timestamp;
import oracle.toplink.essentials.expressions.*;
import oracle.toplink.essentials.internal.helper.*;
import oracle.toplink.essentials.queryframework.*;
import oracle.toplink.essentials.exceptions.*;
import oracle.toplink.essentials.internal.sessions.AbstractRecord;
import oracle.toplink.essentials.internal.sessions.UnitOfWorkImpl;
import oracle.toplink.essentials.internal.sessions.AbstractSession;

/**
 * <p><b>Purpose</b>: Used to allow a single version timestamp to be used for optimistic locking.
 *
 * @since TOPLink/Java 2.0
 */
public class TimestampLockingPolicy extends VersionLockingPolicy {
    protected int retrieveTimeFrom;
    public final static int SERVER_TIME = 1;
    public final static int LOCAL_TIME = 2;

    /**
     * PUBLIC:
     * Create a new TimestampLockingPolicy.
     * Defaults to using the time retrieved from the server.
     */
    public TimestampLockingPolicy() {
        super();
        this.useServerTime();
    }

    /**
     * PUBLIC:
     * Create a new TimestampLockingPolicy.
     * Defaults to using the time retrieved from the server.
     * @param fieldName the field where the write lock value will be stored.
     */
    public TimestampLockingPolicy(String fieldName) {
        super(fieldName);
        this.useServerTime();
    }

    /**
     * INTERNAL:
     * Create a new TimestampLockingPolicy.
     * Defaults to using the time retrieved from the server.
     * @param field the field where the write lock value will be stored.
     */
    public TimestampLockingPolicy(DatabaseField field) {
        super(field);
        this.useServerTime();
    }
    
    /**
     * INTERNAL:
     * This method compares two writeLockValues.
     * The writeLockValues should be non-null and of type java.sql.Timestamp.
     * Returns:
     * -1 if value1 is less (older) than value2;
     *  0 if value1 equals value2;
     *  1 if value1 is greater (newer) than value2.
     * Throws:
     *  NullPointerException if the passed value is null;
     *  ClassCastException if the passed value is of a wrong type.
     */
    public int compareWriteLockValues(Object value1, Object value2) {
        java.sql.Timestamp timestampValue1 = (java.sql.Timestamp)value1;
        java.sql.Timestamp timestampValue2 = (java.sql.Timestamp)value2;
        return timestampValue1.compareTo(timestampValue2);
    }

    /**
     * INTERNAL:
     * Return the default timestamp locking filed java type, default is Timestamp.
     */
    protected Class getDefaultLockingFieldType() {
        return ClassConstants.TIMESTAMP;
    }
    
    /**
     * INTERNAL:
     * This is the base value that is older than all other values, it is used in the place of
     * null in some situations.
     */
    public Object getBaseValue(){
        return new Timestamp(0);
    }
    
    /**
     * INTERNAL:
     * returns the initial locking value
     */
    protected Object getInitialWriteValue(AbstractSession session) {
        if (usesLocalTime()) {
            return new Timestamp(System.currentTimeMillis());
        }
        if (usesServerTime()) {
            AbstractSession readSession = session.getSessionForClass(getDescriptor().getJavaClass());
            while (readSession.isUnitOfWork()) {
                readSession = ((UnitOfWorkImpl)readSession).getParent().getSessionForClass(getDescriptor().getJavaClass());
            }

            return readSession.getDatasourceLogin().getDatasourcePlatform().getTimestampFromServer(session, readSession.getName());
        }
        return null;

    }

    /**
     * INTERNAL:
     * Returns the new Timestamp value.
     */
    public Object getNewLockValue(ModifyQuery query) {
        return getInitialWriteValue(query.getSession());
    }

    /**
     * INTERNAL:
     * Return the value that should be stored in the identity map.  If the value
     * is stored in the object, then return a null.
     */
    public Object getValueToPutInCache(AbstractRecord row, AbstractSession session) {
        if (isStoredInCache()) {
            return session.getDatasourcePlatform().convertObject(row.get(getWriteLockField()), ClassConstants.TIMESTAMP);
        } else {
            return null;
        }
    }

    /**
     * INTERNAL:
     * Return the number of versions different between these objects.
     */
    public int getVersionDifference(Object currentValue, Object domainObject, Vector primaryKeys, AbstractSession session) {
        java.sql.Timestamp writeLockFieldValue;
        java.sql.Timestamp newWriteLockFieldValue = (java.sql.Timestamp)currentValue;
        if (newWriteLockFieldValue == null) {
            return 0;//merge it as either the object is new or being forced merged.
        }
        if (isStoredInCache()) {
            writeLockFieldValue = (java.sql.Timestamp)session.getIdentityMapAccessor().getWriteLockValue(primaryKeys, domainObject.getClass());
        } else {
            writeLockFieldValue = (java.sql.Timestamp)lockValueFromObject(domainObject);
        }
        if ((writeLockFieldValue != null) && !(newWriteLockFieldValue.after(writeLockFieldValue))) {
            return 0;
        }

        //if the new value is newer then perform the update.  Eventually this will be changed to
        //record the old version and compare that for equality
        return 2;
    }

    /**
     * INTERNAL:
     * This method will return the optimistic lock value for the object
     */
    public Object getWriteLockValue(Object domainObject, java.util.Vector primaryKey, AbstractSession session) {
        java.sql.Timestamp writeLockFieldValue = null;
        if (isStoredInCache()) {
            writeLockFieldValue = (java.sql.Timestamp)session.getIdentityMapAccessor().getWriteLockValue(primaryKey, domainObject.getClass());
        } else {
            //CR#2281 notStoredInCache prevent ClassCastException
            Object lockValue = lockValueFromObject(domainObject);
            if (lockValue != null) {
                if (lockValue instanceof java.sql.Timestamp) {
                    writeLockFieldValue = (java.sql.Timestamp)lockValueFromObject(domainObject);
                } else {
                    throw OptimisticLockException.needToMapJavaSqlTimestampWhenStoredInObject();
                }
            }
        }
        return writeLockFieldValue;
    }

    /**
     * INTERNAL:
     * Retrun an expression that updates the write lock
     */
    public Expression getWriteLockUpdateExpression(ExpressionBuilder builder) {
        return builder.value(getInitialWriteValue(builder.getSession()));
    }

    /**
     * INTERNAL:
     * Timestamp versioning should not be able to do this.  Override the superclass behaviour.
     */
    protected Number incrementWriteLockValue(Number numberValue) {
        return null;
    }

    /**
     * INTERNAL:
     * Update the parent write lock value if the objectChangeSet's is greater.
     */
    public boolean isChildWriteLockValueGreater(AbstractSession session, java.util.Vector primaryKey, Class original, ObjectChangeSet changeSet) {
        if (isStoredInCache()) {
            // If this uow changed the object the version must be updated,
            // we can check this by ensuring our value is greater than our parent's.
            java.sql.Timestamp writeLockValue = (java.sql.Timestamp)changeSet.getWriteLockValue();
            java.sql.Timestamp parentValue = (java.sql.Timestamp)session.getIdentityMapAccessor().getWriteLockValue(primaryKey, original);
            if (writeLockValue != null) {// This occurs if the object was deleted
                if ((parentValue == null) || parentValue.before(writeLockValue)) {// Check parent value is less than child
                    return true;
                }
            }
        }

        return false;
    }

    /**
     * INTERNAL:
     * Update the parent write lock value if the unit of works has been incremented.
     */
    public boolean isChildWriteLockValueGreater(UnitOfWorkImpl uow, java.util.Vector primaryKey, Class original) {
        if (isStoredInCache()) {
            // If this uow changed the object the version must be updated,
            // we can check this by ensuring our value is greater than our parent's.
            java.sql.Timestamp writeLockValue = (java.sql.Timestamp)uow.getIdentityMapAccessor().getWriteLockValue(primaryKey, original);
            java.sql.Timestamp parentValue = (java.sql.Timestamp)uow.getParent().getIdentityMapAccessor().getWriteLockValue(primaryKey, original);
            if (writeLockValue != null) {// This occurs if the object was deleted
                if ((parentValue == null) || parentValue.before(writeLockValue)) {// Check parent value is less than child
                    return true;
                }
            }
        }

        return false;
    }

    /**
     * INTERNAL:
     * Compares the value with the value from the object (or cache).
     * Will return true if the object is newer.
     */
    public boolean isNewerVersion(Object currentValue, Object domainObject, java.util.Vector primaryKey, AbstractSession session) {
        java.sql.Timestamp writeLockFieldValue;
        java.sql.Timestamp newWriteLockFieldValue = (java.sql.Timestamp)currentValue;
        if (isStoredInCache()) {
            writeLockFieldValue = (java.sql.Timestamp)session.getIdentityMapAccessor().getWriteLockValue(primaryKey, domainObject.getClass());
        } else {
            writeLockFieldValue = (java.sql.Timestamp)lockValueFromObject(domainObject);
        }
        // bug 6342382: object's lock value is null, it is NOT newer than any newWriteLockFieldValue.
        if(writeLockFieldValue == null) {
            return false;
        }
        // 2.5.1.6 if the write lock value is null, then what ever we have is treated as newer.
        if (newWriteLockFieldValue == null) {
            return true;
        }
        if (!(newWriteLockFieldValue.after(writeLockFieldValue))) {
            return false;
        }
        return true;
    }

    /**
     * INTERNAL:
     * Compares the value from the row and from the object (or cache).
     * Will return true if the object is newer than the row.
     */
    public boolean isNewerVersion(AbstractRecord databaseRow, Object domainObject, java.util.Vector primaryKey, AbstractSession session) {
        java.sql.Timestamp writeLockFieldValue;
        java.sql.Timestamp newWriteLockFieldValue = (java.sql.Timestamp)session.getDatasourcePlatform().convertObject(databaseRow.get(getWriteLockField()), ClassConstants.TIMESTAMP);
        if (isStoredInCache()) {
            writeLockFieldValue = (java.sql.Timestamp)session.getIdentityMapAccessor().getWriteLockValue(primaryKey, domainObject.getClass());
        } else {
            writeLockFieldValue = (java.sql.Timestamp)lockValueFromObject(domainObject);
        }
        // bug 6342382: object's lock value is null, it is NOT newer than any newWriteLockFieldValue.
        if(writeLockFieldValue == null) {
            return false;
        }
        // 2.5.1.6 if the write lock value is null, then what ever we have is treated as newer.
        if (newWriteLockFieldValue == null) {
            return true;
        }
        if (!(newWriteLockFieldValue.after(writeLockFieldValue))) {
            return false;
        }
        return true;
    }

    /**
     * PUBLIC:
     * Set if policy uses server time.
     */
    public void setUsesServerTime(boolean usesServerTime) {
        if (usesServerTime) {
            useServerTime();
        } else {
            useLocalTime();
        }
    }

    /**
     * PUBLIC:
     * set this policy to get the time from the local machine.
     */
    public void useLocalTime() {
        retrieveTimeFrom = LOCAL_TIME;
    }

    /**
     * PUBLIC:
     * set this policy to get the time from the server.
     */
    public void useServerTime() {
        retrieveTimeFrom = SERVER_TIME;
    }

    /**
     * PUBLIC:
     * Return true if policy uses local time.
     */
    public boolean usesLocalTime() {
        return (retrieveTimeFrom == LOCAL_TIME);
    }

    /**
     * PUBLIC:
     * Return true if policy uses server time.
     */
    public boolean usesServerTime() {
        return (retrieveTimeFrom == SERVER_TIME);
    }
}
