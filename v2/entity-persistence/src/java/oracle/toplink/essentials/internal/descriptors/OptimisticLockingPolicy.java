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
package oracle.toplink.essentials.internal.descriptors;

import java.io.Serializable;
import java.util.Vector;
import oracle.toplink.essentials.expressions.*;
import oracle.toplink.essentials.internal.helper.DatabaseField;
import oracle.toplink.essentials.internal.sessions.ObjectChangeSet;
import oracle.toplink.essentials.queryframework.*;
import oracle.toplink.essentials.internal.sessions.AbstractRecord;
import oracle.toplink.essentials.internal.sessions.UnitOfWorkImpl;
import oracle.toplink.essentials.internal.sessions.AbstractSession;
import oracle.toplink.essentials.descriptors.ClassDescriptor;

public interface OptimisticLockingPolicy extends Cloneable, Serializable {

    /**
     * INTERNAL:
     * Add update fields for template row.
     * These are any unmapped fields required to write in an update.
     *
     * #see this method in VersionLockingPolicy
     */
    public void addLockFieldsToUpdateRow(AbstractRecord databaseRow, AbstractSession session);

    /**
     * INTERNAL:
     * The method should update the translation row with the
     * correct write lock values. This method is called on a delete.
     *
     * #see this method in VersionLockingPolicy
     */
    public void addLockValuesToTranslationRow(ObjectLevelModifyQuery query);

    /**
     * INTERNAL:
     * When given an expression, this method will return a new expression with
     * the optimistic locking values included.  This expression will be used
     * in a delete call.
     *
     * #see this method in VersionLockingPolicy
     */
    public Expression buildDeleteExpression(oracle.toplink.essentials.internal.helper.DatabaseTable table, Expression mainExpression, AbstractRecord row);

    /**
     * INTERNAL:
     * When given an expression, this method will return a new expression with
     * the optimistic locking values included.  This expression will be used in
     * an update call.
     *
     * #see this method in VersionLockingPolicy
     */
    public Expression buildUpdateExpression(oracle.toplink.essentials.internal.helper.DatabaseTable table, Expression mainExpression, AbstractRecord translationRow, AbstractRecord modifyRow);

    public Object clone();

    /**
     * INTERNAL:
     * This method compares two writeLockValues.
     * The writeLockValues should be non-null and of the correct type.
     * Returns:
     * -1 if value1 is less (older) than value2;
     *  0 if value1 equals value2;
     *  1 if value1 is greater (newer) than value2.
     * Throws:
     *  NullPointerException if the passed value is null;
     *  ClassCastException if the passed value is of a wrong type.
     */
    public int compareWriteLockValues(Object value1, Object value2);

    /**
     * INTERNAL:
     * This is the base value that is older than all other values, it is used in the place of
     * null in some situations.
     */
    abstract public Object getBaseValue();

    /**
     * INTERNAL:
     * Return the value that should be stored in the identity map.
     * If the value is not stored in the cache, then return a null.
     *
     * #see this method in VersionLockingPolicy
     */
    public Object getValueToPutInCache(AbstractRecord row, AbstractSession session);

    /**
     * PUBLIC:
     * Return the number of versions different between these objects.
     */
    public int getVersionDifference(Object currentValue, Object domainObject, Vector primaryKeys, AbstractSession session);

    /**
     * INTERNAL:
     * Return the write lock field.
    *
    * #see this method in VersionLockingPolicy
     */
    public DatabaseField getWriteLockField();

    /**
     * INTERNAL:
     * This method will return the optimistic lock value for the object
     *
     * #see this method in VersionLockingPolicy
     */
    public Object getWriteLockValue(Object domainObject, java.util.Vector primaryKey, AbstractSession session);

    /**
       * INTERNAL:
       * This method will return an expression that is used to update its optimistic
     * locking field.
     *
     * #see this method in VersionLockingPolicy
       */
    public Expression getWriteLockUpdateExpression(ExpressionBuilder builder);

    /**
     * INTERNAL:
     * It is responsible for initializing the policy.
     *
     * #see this method in VersionLockingPolicy
     */
    public void initialize(AbstractSession session);

    /**
     * INTERNAL:
     * Responsible for pre-initializing.
     *
     * #see this method in VersionLockingPolicy
     */
    public void initializeProperties();
    
    /**
     * INTERNAL:
     * Update the parent write lock value if the changeSet's is newer
     */
    public boolean isChildWriteLockValueGreater(AbstractSession session, java.util.Vector primaryKey, Class original, ObjectChangeSet changeSet);

    /**
     * INTERNAL:
     * Update the parent write lock value if the unit of works has been incremented
     */
    public boolean isChildWriteLockValueGreater(UnitOfWorkImpl uow, java.util.Vector primaryKey, Class original);

    /**
     * INTERNAL:
     * Returns true if the value stored with the domainObject is more recent
     * than the value .  Returns false otherwise.
     *
     * #see this method in VersionLockingPolicy
     */
    public boolean isNewerVersion(Object currentValue, Object domainObject, java.util.Vector primaryKey, AbstractSession session);

    /**
     * INTERNAL:
     * Returns true if the value stored with the domainObject is more recent
     * than the value in the row.  Returns false otherwise.
     * NOTE: This method will only be called if the shouldOnlyRefreshCacheIfNewerVersion()
     * flag is set on descriptor.
     *
     * #see this method in VersionLockingPolicy
     */
    public boolean isNewerVersion(AbstractRecord databaseRow, Object domainObject, java.util.Vector primaryKey, AbstractSession session);

    /**
     * INTERNAL:
     * This method should merge changes from the parent into the child.
     *
     * #see this method in VersionLockingPolicy
     */
    public void mergeIntoParentCache(UnitOfWorkImpl uow, java.util.Vector primaryKey, Object object);

    /**
     * INTERNAL:
     * provide a way to set the descriptor for this policy
     */
    public void setDescriptor(ClassDescriptor descriptor);

    /**
     * INTERNAL:
     * Add the initial right lock values to the modify
     * row in the query. This method will only be called
     * on insert.
     *
     * #see this method in VersionLockingPolicy
     */
    public void setupWriteFieldsForInsert(ObjectLevelModifyQuery query);

    /**
     * INTERNAL:
     * This method should update the translation row, the modify
     * row and the domain object with th lock value.
     *
     * #see this method in VersionLockingPolicy
     */
    public void updateRowAndObjectForUpdate(ObjectLevelModifyQuery query, Object object);

    public void validateDelete(int rowCount, Object object, WriteObjectQuery query);

    public void validateUpdate(int rowCount, Object object, WriteObjectQuery query);

    /**
     * INTERNAL:
     * Prepare fetch group for read query
     */
    public void prepareFetchGroupForReadQuery(FetchGroup fetchGroup, ObjectLevelReadQuery query);
}
