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
package oracle.toplink.essentials.internal.indirection;

import java.util.*;
import java.rmi.server.ObjID;
import oracle.toplink.essentials.exceptions.ValidationException;
import oracle.toplink.essentials.indirection.*;
import oracle.toplink.essentials.mappings.*;
import oracle.toplink.essentials.logging.SessionLog;
import oracle.toplink.essentials.internal.sessions.UnitOfWorkImpl;
import oracle.toplink.essentials.descriptors.ClassDescriptor;

/**
 * A UnitOfWorkValueHolder is put in a clone object.
 * It wraps the value holder in the original object to delay
 * cloning the attribute in a unit of work until it is
 * needed by the application.
 * This value holder is used only in the unit of work.
 *
 * @author    Sati
 */
public abstract class UnitOfWorkValueHolder extends DatabaseValueHolder {

    /** The value holder in the original object. */
    protected transient ValueHolderInterface wrappedValueHolder;

    /** The mapping for the attribute. */
    protected transient DatabaseMapping mapping;

    /** The value holder stored in the backup copy, should not be transient. */
    protected ValueHolder backupValueHolder;

    /** These cannot be transient because they are required for a remote unit of work.
    When the remote uow is serialized to the server to be committed, these
    are used to reconstruct the value holder on the server.
    They should be null for non-remote sessions. */
    protected UnitOfWorkImpl remoteUnitOfWork;
    protected Object sourceObject;

    /** This attribute is used specifically for relationship support.  It mimicks the
     * sourceObject attribute which is used for RemoteValueholder
     */
    protected transient Object relationshipSourceObject;
    protected String sourceAttributeName;
    protected ObjID wrappedValueHolderRemoteID;

    protected UnitOfWorkValueHolder(ValueHolderInterface attributeValue, Object clone, DatabaseMapping mapping, UnitOfWorkImpl unitOfWork) {
        this.wrappedValueHolder = attributeValue;
        this.mapping = mapping;
        this.session = unitOfWork;
        this.sourceAttributeName = mapping.getAttributeName();
        this.relationshipSourceObject = clone;
    }

    /**
     * Backup the clone attribute value.
     */
    protected abstract Object buildBackupCloneFor(Object cloneAttributeValue);

    /**
     * Clone the original attribute value.
     */
    public abstract Object buildCloneFor(Object originalAttributeValue);

    protected ValueHolder getBackupValueHolder() {
        return backupValueHolder;
    }

    protected DatabaseMapping getMapping() {
        return mapping;
    }

    protected UnitOfWorkImpl getRemoteUnitOfWork() {
        return remoteUnitOfWork;
    }

    protected String getSourceAttributeName() {
        return sourceAttributeName;
    }

    protected Object getSourceObject() {
        return sourceObject;
    }

    protected Object getRelationshipSourceObject() {
        return this.relationshipSourceObject;
    }

    protected UnitOfWorkImpl getUnitOfWork() {
        return (UnitOfWorkImpl)getSession();
    }

    /**
     * This is used for a remote unit of work.
     * If the value holder is sent back to the server uninstantiated and
     * it needs to be instantiated, then we must find the original
     * object and get the appropriate attribute from it.
     */
    protected Object getValueFromServerObject() {
        setSession(getRemoteUnitOfWork());
        Vector primaryKey = getSession().keyFromObject(getSourceObject());
        Object originalObject = getUnitOfWork().getParent().getIdentityMapAccessor().getFromIdentityMap(primaryKey, getSourceObject().getClass());
        if (originalObject == null) {
            originalObject = getUnitOfWork().getParent().readObject(getSourceObject());
        }
        ClassDescriptor descriptor = getSession().getDescriptor(originalObject);
        DatabaseMapping mapping = descriptor.getObjectBuilder().getMappingForAttributeName(getSourceAttributeName());
        setMapping(mapping);
        return getMapping().getRealAttributeValueFromObject(originalObject, getSession());
    }

    /**
     * a.k.a getValueFromWrappedValueholder.
     * The old name is no longer correct, as query based valueholders are now
     * sometimes triggered directly without triggering the underlying valueholder.
     */
    protected Object instantiateImpl() {
        if (getWrappedValueHolder() instanceof DatabaseValueHolder) {
            // Bug 3835202 - Ensure access to valueholders is thread safe.  Several of the methods
            // called below are not threadsafe alone.
            synchronized(getWrappedValueHolder()){
                DatabaseValueHolder wrapped = (DatabaseValueHolder)getWrappedValueHolder();
                UnitOfWorkImpl unitOfWork = getUnitOfWork();
                if (!wrapped.isEasilyInstantiated()) {
                    if (wrapped.isPessimisticLockingValueHolder()) {
                        if (!unitOfWork.getCommitManager().isActive() && !unitOfWork.wasTransactionBegunPrematurely()) {
                            unitOfWork.beginEarlyTransaction();
                        }
                        unitOfWork.log(SessionLog.FINEST, SessionLog.TRANSACTION, "instantiate_pl_relationship");
                    }
                    if (unitOfWork.getCommitManager().isActive() || unitOfWork.wasTransactionBegunPrematurely()) {
                        // At this point the wrapped valueholder is not triggered,
                        // and we are in transaction.  So just trigger the
                        // UnitOfWork valueholder on the UnitOfWork only.
                        return wrapped.instantiateForUnitOfWorkValueHolder(this);
                    }
                }
            }
        }
        Object originalAttributeValue = getWrappedValueHolder().getValue();
        return buildCloneFor(originalAttributeValue);
    }

    /**
     * INTERNAL:
     * Answers if this valueholder is easy to instantiate.
     * @return true if getValue() won't trigger a database read.
     */
    public boolean isEasilyInstantiated() {
        return isInstantiated() || ((getWrappedValueHolder() != null) && (getWrappedValueHolder() instanceof DatabaseValueHolder) && ((DatabaseValueHolder)getWrappedValueHolder()).isEasilyInstantiated());
    }

    /**
     * INTERNAL:
     * Answers if this valueholder is a pessimistic locking one.  Such valueholders
     * are special in that they can be triggered multiple times by different
     * UnitsOfWork.  Each time a lock query will be issued.  Hence even if
     * instantiated it may have to be instantiated again, and once instantatiated
     * all fields can not be reset.
     */
    public boolean isPessimisticLockingValueHolder() {
        // This abstract method needs to be implemented but is not meaningfull for
        // this subclass.
        return ((getWrappedValueHolder() != null) && (getWrappedValueHolder() instanceof DatabaseValueHolder) && ((DatabaseValueHolder)getWrappedValueHolder()).isPessimisticLockingValueHolder());
    }

    public ValueHolderInterface getWrappedValueHolder() {
        return wrappedValueHolder;
    }

    /**
     * returns wrapped ValueHolder ObjID if available
     */
    public ObjID getWrappedValueHolderRemoteID() {
        return this.wrappedValueHolderRemoteID;
    }

    /**
     * Used to determine if this is a remote uow value holder that was serialized to the server.
     * It has no reference to its wrapper value holder, so must find its original object to be able to instantiate.
     */
    public boolean isSerializedRemoteUnitOfWorkValueHolder() {
        return (getRemoteUnitOfWork() != null) && (getRemoteUnitOfWork().getParent() != null) && (getWrappedValueHolder() == null);
    }
    
    /**
     * Get the value from the wrapped value holder, instantiating it
     * if necessary, and clone it.
     */
    protected Object instantiate() {
        UnitOfWorkImpl unitOfWork;
        if (isSerializedRemoteUnitOfWorkValueHolder()) {
            unitOfWork = getRemoteUnitOfWork();
        } else {
            unitOfWork = getUnitOfWork();
        }
        if (unitOfWork == null){
            throw ValidationException.instantiatingValueholderWithNullSession();
        }
        if (unitOfWork.isAfterWriteChangesButBeforeCommit()) {
            throw ValidationException.illegalOperationForUnitOfWorkLifecycle(unitOfWork.getLifecycle(), "ValueHolder.instantiate()");
        }

        Object originalAttributeValue;
        Object cloneAttributeValue;

        // the wrapped value holder is transient, so it will be null for a remote UOW
        if (isSerializedRemoteUnitOfWorkValueHolder()) {
            originalAttributeValue = getValueFromServerObject();
            cloneAttributeValue = buildCloneFor(originalAttributeValue);
        } else {
            cloneAttributeValue = instantiateImpl();
        }

        // Set the value in the backup clone also.
        // In some cases we may want to force instantiation before the backup is built
        if (getBackupValueHolder() != null) {
            getBackupValueHolder().setValue(buildBackupCloneFor(cloneAttributeValue));
        }
        return cloneAttributeValue;
    }

    /**
     * Triggers UnitOfWork valueholders directly without triggering the wrapped
     * valueholder (this).
     * <p>
     * When in transaction and/or for pessimistic locking the UnitOfWorkValueHolder
     * needs to be triggered directly without triggering the wrapped valueholder.
     * However only the wrapped valueholder knows how to trigger the indirection,
     * i.e. it may be a batchValueHolder, and it stores all the info like the row
     * and the query.
     */
    public Object instantiateForUnitOfWorkValueHolder(UnitOfWorkValueHolder unitOfWorkValueHolder) {
        // This abstract method needs to be implemented but is not meaningfull for
        // this subclass.
        return instantiate();
    }

    /**
     * Releases a wrapped valueholder privately owned by a particular unit of work.
     * <p>
     * When unit of work clones are built directly from rows no object in the shared
     * cache points to this valueholder, so it can store the unit of work as its
     * session.  However once that UnitOfWork commits and the valueholder is merged
     * into the shared cache, the session needs to be reset to the root session, ie.
     * the server session.
     */
    public void releaseWrappedValueHolder() {
        // On UnitOfWork dont want to do anything.
        return;
    }

    /**
     * Reset all the fields that are not needed after instantiation.
     */
    protected void resetFields() {
        //do nothing.  nothing should be reset to null;
    }

    public void setBackupValueHolder(ValueHolder backupValueHolder) {
        this.backupValueHolder = backupValueHolder;
    }

    protected void setMapping(DatabaseMapping mapping) {
        this.mapping = mapping;
    }

    protected void setRemoteUnitOfWork(UnitOfWorkImpl remoteUnitOfWork) {
        this.remoteUnitOfWork = remoteUnitOfWork;
    }

    protected void setSourceAttributeName(String name) {
        sourceAttributeName = name;
    }

    protected void setSourceObject(Object sourceObject) {
        this.sourceObject = sourceObject;
    }

    protected void setRelationshipSourceObject(Object relationshipSourceObject) {
        this.relationshipSourceObject = relationshipSourceObject;
    }

    protected void setWrappedValueHolder(DatabaseValueHolder valueHolder) {
        wrappedValueHolder = valueHolder;
    }
}
