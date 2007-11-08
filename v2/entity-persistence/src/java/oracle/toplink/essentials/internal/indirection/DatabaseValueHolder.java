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

import java.io.*;
import oracle.toplink.essentials.internal.helper.*;
import oracle.toplink.essentials.indirection.*;
import oracle.toplink.essentials.exceptions.*;
import oracle.toplink.essentials.internal.localization.*;
import oracle.toplink.essentials.internal.sessions.AbstractRecord;
import oracle.toplink.essentials.internal.sessions.AbstractSession;

/**
 * DatabaseValueHolder wraps a database-stored object and implements
 * behavior to access it. The object is read only once from database
 * after which is cached for faster access.
 *
 * @see ValueHolderInterface
 * @author    Dorin Sandu
 */
public abstract class DatabaseValueHolder implements WeavedAttributeValueHolderInterface, Cloneable, Serializable {

    /** Stores the object after it is read from the database. */
    protected Object value;

    /** Indicates whether the object has been read from the database or not. */
    protected boolean isInstantiated;

    /** Stores the session for the database that contains the object. */
    protected transient AbstractSession session;

    /** Stores the row representation of the object. */
    protected AbstractRecord row;
  
    /**
     * The variable below is used as part of the implementation of WeavedAttributeValueHolderInterface
     * It is used to track whether a valueholder that has been weaved into a class is coordinated
     * with the underlying property
     * Set internally in TopLink when the state of coordination between a weaved valueholder and the underlying property is known
     */
    protected boolean isCoordinatedWithProperty = false;

    public Object clone() {
        try {
            return super.clone();
        } catch (CloneNotSupportedException exception) {
            throw new InternalError();
        }
    }

    /**
     * Return the row.
     */
    public AbstractRecord getRow() {
        return row;
    }

    /**
     * Return the session.
     */
    public AbstractSession getSession() {
        return session;
    }

    /**
     * Return the object.
     */
    public synchronized Object getValue() {
        if (!isInstantiated()) {
            // The value must be set directly because the setValue can also cause instatiation under UOW.
            privilegedSetValue(instantiate());
            setInstantiated();
            resetFields();
        }
        return value;
    }

    /**
     * Instantiate the object.
     */
    protected abstract Object instantiate() throws DatabaseException;

    /**
     * Triggers UnitOfWork valueholders directly without triggering the wrapped
     * valueholder (this).
     * <p>
     * When in transaction and/or for pessimistic locking the UnitOfWorkValueHolder
     * needs to be triggered directly without triggering the wrapped valueholder.
     * However only the wrapped valueholder knows how to trigger the indirection,
     * i.e. it may be a batchValueHolder, and it stores all the info like the row
     * and the query.
     * Note: Implementations of this method are not necessarily thread-safe.  They must 
     * be used in a synchronizaed manner
     */
    public abstract Object instantiateForUnitOfWorkValueHolder(UnitOfWorkValueHolder unitOfWorkValueHolder);

    /**
     * This method is used as part of the implementation of WeavedAttributeValueHolderInterface
     * It is used to check whether a valueholder that has been weaved into a class is coordinated
     * with the underlying property
     */
    public boolean isCoordinatedWithProperty(){
        return isCoordinatedWithProperty;
    }
    
    /**
     * This method is used as part of the implementation of WeavedAttributeValueHolderInterface.
     * 
     * A DatabaseValueHolder is set up by TopLink and will never be a newly weaved valueholder.
     * As a result, this method is stubbed out.
     */
    public boolean isNewlyWeavedValueHolder(){
        return false;
    }
    
    /**
     * INTERNAL:
     * Answers if this valueholder is easy to instantiate.
     * @return true if getValue() won't trigger a database read.
     */
    public boolean isEasilyInstantiated() {
        return isInstantiated();
    }

    /**
     * Return a boolean indicating whether the object
     * has been read from the database or not.
     */
    public boolean isInstantiated() {
        return isInstantiated;
    }

    /**
     * Answers if this valueholder is a pessimistic locking one.  Such valueholders
     * are special in that they can be triggered multiple times by different
     * UnitsOfWork.  Each time a lock query will be issued.  Hence even if
     * instantiated it may have to be instantiated again, and once instantatiated
     * all fields can not be reset.
     * Note: Implementations of this method are not necessarily thread-safe.  They must 
     * be used in a synchronizaed manner
     */
    public abstract boolean isPessimisticLockingValueHolder();

    /**
     * Answers if this valueholder is referenced only by a UnitOfWork valueholder.
     * I.e. it was built in valueFromRow which was called by buildCloneFromRow.
     * <p>
     * Sometimes in transaction a UnitOfWork clone, and all valueholders, are built
     * directly from the row; however a UnitOfWorkValueHolder does not know how to
     * instantiate itself so wraps this which does.
     * <p>
     * On a successful merge must be released to the session cache with
     * releaseWrappedValueHolder.
     */
    protected boolean isTransactionalValueHolder() {
        return ((session != null) && session.isUnitOfWork());
    }
    
    /**
     * Used to determine if this is a remote uow value holder that was serialized to the server.
     * It has no reference to its wrapper value holder, so must find its original object to be able to instantiate.
     */
    public boolean isSerializedRemoteUnitOfWorkValueHolder() {
        return false;
    }

    /**
     * Set the object. This is used only by the privileged methods. One must be very careful in using this method.
     */
    public void privilegedSetValue(Object value) {
        this.value = value;
        isCoordinatedWithProperty = false;
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
        AbstractSession session = getSession();
        if ((session != null) && session.isUnitOfWork()) {
            setSession(session.getRootSession(null));
        }
    }

    /**
     * Reset all the fields that are not needed after instantiation.
     */
    protected void resetFields() {
        setRow(null);
        setSession(null);
    }
    
    /**
     * This method is used as part of the implementation of WeavedAttributeValueHolderInterface
     * It is used internally by TopLink to set whether a valueholder that has been weaved into a class is coordinated
     * with the underlying property
     */
    public void setIsCoordinatedWithProperty(boolean coordinated){
        this.isCoordinatedWithProperty = coordinated;
    }

    /**
     * This method is used as part of the implementation of WeavedAttributeValueHolderInterface
     * 
     * A DatabaseValueHolder is set up by TopLink and will never be a newly weaved valueholder 
     * As a result, this method is stubbed out.
     */
    public void setIsNewlyWeavedValueHolder(boolean isNew){
    }
    
    /**
     * Set the instantiated flag to true.
     */
    public void setInstantiated() {
        isInstantiated = true;
    }

    /**
     * Set the row.
     */
    public void setRow(AbstractRecord row) {
        this.row = row;
    }

    /**
     * Set the session.
     */
    public void setSession(AbstractSession session) {
        this.session = session;
    }

    /**
     * Set the instantiated flag to false.
     */
    public void setUninstantiated() {
        isInstantiated = false;
    }

    /**
     * Set the object.
     */
    public void setValue(Object value) {
        this.value = value;
        setInstantiated();
    }

    public String toString() {
        if (isInstantiated()) {
            return "{" + getValue() + "}";
        } else {
            return "{" + Helper.getShortClassName(getClass()) + ": " + ToStringLocalization.buildMessage("not_instantiated", (Object[])null) + "}";
        }
    }
}
