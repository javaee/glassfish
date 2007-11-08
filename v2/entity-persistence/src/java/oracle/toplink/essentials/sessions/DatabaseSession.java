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
package oracle.toplink.essentials.sessions;

import java.util.*;
import oracle.toplink.essentials.exceptions.*;
import oracle.toplink.essentials.descriptors.ClassDescriptor;
import oracle.toplink.essentials.platform.server.ServerPlatform;
import oracle.toplink.essentials.sequencing.SequencingControl;

/**
 * <p>
 * <b>Purpose</b>: Add login and configuration API to that of Session.
 * This interface is to be used during the creation and login of the session only.
 * The Session interface should be used after login for normal reading/writing.
 */
public interface DatabaseSession extends Session {

    /**
     * PUBLIC:
     * Add the descriptor to the session.
     * All persistent classes must have a descriptor registered for them with the session.
     * It is best to add the descriptors before login, if added after login the order in which
     * descriptors are added is dependant on inheritice and references unless the addDescriptors
     * method is used.
     *
     * @see #addDescriptors(Vector)
     * @see #addDescriptors(Project)
     */
    public void addDescriptor(ClassDescriptor descriptor);

    /**
     * PUBLIC:
     * Add the descriptors to the session.
     * All persistent classes must have a descriptor registered for them with the session.
     * This method allows for a batch of descriptors to be added at once so that TopLink
     * can resolve the dependancies between the descriptors and perform initialization optimially.
     */
    public void addDescriptors(Vector descriptors);

    /**
     * PUBLIC:
     * Add the descriptors to the session from the Project.
     * This can be used to combine the descriptors from multiple projects into a single session.
     * This can be called after the session has been connected as long as there are no external dependencies.
     */
    public void addDescriptors(oracle.toplink.essentials.sessions.Project project);

    /**
     * PUBLIC:
     * Begin a transaction on the database.
     * This allows a group of database modification to be commited or rolledback as a unit.
     * All writes/deletes will be sent to the database be will not be visible to other users until commit.
     * Although databases do not allow nested transaction,
     * TopLink supports nesting through only committing to the database on the outer commit.
     *
     * @exception DatabaseException if the database connection is lost or the begin is rejected.
     *
     * @see #isInTransaction()
     */
    public void beginTransaction() throws DatabaseException;

    /**
     * PUBLIC:
     * Commit the active database transaction.
     * This allows a group of database modification to be commited or rolledback as a unit.
     * All writes/deletes will be sent to the database be will not be visible to other users until commit.
     * Although databases do not allow nested transaction,
     * TopLink supports nesting through only committing to the database on the outer commit.
     *
     * @exception DatabaseException most databases validate changes as they are done,
     * normally errors do not occur on commit unless the disk fails or the connection is lost.
     * @exception ConcurrencyException if this session is not within a transaction.
     */
    public void commitTransaction() throws DatabaseException;

    /**
     * PUBLIC:
     * delete all of the objects and all of their privately owned parts in the database.
     * The allows for a group of objects to be deleted as a unit.
     * The objects will be deleted through a single transactions.
     *
     * @exception DatabaseException if an error occurs on the database,
     * these include constraint violations, security violations and general database erros.
     * @exception OptimisticLockException if the object's descriptor is using optimistic locking and
     * the object has been updated or deleted by another user since it was last read.
     */
    public void deleteAllObjects(Collection domainObjects);

    /**
     * PUBLIC:
     * delete all of the objects and all of their privately owned parts in the database.
     * The allows for a group of objects to be deleted as a unit.
     * The objects will be deleted through a single transactions.
     *
     * @exception DatabaseException if an error occurs on the database,
     * these include constraint violations, security violations and general database erros.
     * @exception OptimisticLockException if the object's descriptor is using optimistic locking and
     * the object has been updated or deleted by another user since it was last read.
     */
    public void deleteAllObjects(Vector domainObjects);

    /**
     * PUBLIC:
     * Delete the object and all of its privately owned parts from the database.
     * The delete operation can be customized through using a delete query.
     *
     * @see oracle.toplink.essentials.queryframework.DeleteObjectQuery
     */
    public Object deleteObject(Object domainObject) throws DatabaseException, OptimisticLockException;

    /**
     * PUBLIC:
     * Insert the object and all of its privately owned parts into the database.
     * Insert should only be used if the application knows that the object is new,
     * otherwise writeObject should be used.
     * The insert operation can be customized through using an insert query.
     *
     * @see oracle.toplink.essentials.queryframework.InsertObjectQuery
     * @see #writeObject(Object)
     */
    public Object insertObject(Object domainObject) throws DatabaseException;

    /**
     * PUBLIC:
     * Return if the session is currently in the progress of a database transaction.
     * Because nested transactions are allowed check if the transaction mutex has been aquired.
     */
    public boolean isInTransaction();

    /**
     * PUBLIC:
     * Set the server platform defining server-specific behaviour for the receiver (Oc4j, WLS, ... ).
     *
     * This is not permitted after the session is logged in.
     *
     * If the user wants a different external transaction controller class or
     * to provide some different behaviour than the provided ServerPlatform(s), we recommend
     * subclassing oracle.toplink.essentials.platform.server.ServerPlatformBase (or a subclass),
     * and overriding:
     *
     * ServerPlatformBase.getExternalTransactionControllerClass()
     * ServerPlatformBase.registerMBean()
     * ServerPlatformBase.unregisterMBean()
     *
     * for the desired behaviour.
     *
     * @see oracle.toplink.essentials.platform.server.ServerPlatformBase
     */
    public void setServerPlatform(ServerPlatform newServerPlatform);

    /**
     * PUBLIC:
     * Answer the server platform defining server-specific behaviour for the receiver (Oc4j, WLS, ...).
     *
      * If the user wants a different external transaction controller class or
     * to provide some different behaviour than the provided ServerPlatform(s), we recommend
     * subclassing oracle.toplink.essentials.platform.server.ServerPlatformBase (or a subclass),
     * and overriding:
     *
     * ServerPlatformBase.getExternalTransactionControllerClass()
     * ServerPlatformBase.registerMBean()
     * ServerPlatformBase.unregisterMBean()
     *
     * for the desired behaviour.
     *
     * @see oracle.toplink.essentials.platform.server.ServerPlatformBase
    */
    public ServerPlatform getServerPlatform();

    /**
     * PUBLIC:
     * Return  SequencingControl which used for sequencing setup and
     * customization including management of sequencing preallocation.
     */
    public SequencingControl getSequencingControl();
        
    /**
     * PUBLIC:
     * Connect to the database using the predefined login.
     * The login must have been assign when or after creating the session.
     *
     * @see #login(Login)
     */
    public void login() throws DatabaseException;

    /**
     * PUBLIC:
     * Connect to the database using the given user name and password.
     * The additional login information must have been preset in the session's login attribute.
     * This is the login that should be used if each user has their own id,
     * but all users share the same database configuration.
     * Under this login mode the password should not stay withint the login definition after login.
     */
    public void login(String userName, String password) throws DatabaseException;

    /**
     * PUBLIC:
     * Connect to the database using the given login.
     * The login may also the preset and the login() protocol called.
     * This is the login should only be used if each user has their own database configuration.
     * Under this login mode the password should not stay withint the login definition after login.
     */
    public void login(Login login) throws DatabaseException;

    /**
     * PUBLIC:
     * Disconnect from the database.
     *
     * @exception TopLinkException if a transaction is active, you must rollback any active transaction before logout.
     * @exception DatabaseException the database will also raise an error if their is an active transaction,
     * or a general error occurs.
     */
    public void logout() throws DatabaseException;

    /**
     * PUBLIC:
     * Refresh the attributes of the object and of all of its private parts from the database.
     * The object will be pessimisticly locked on the database for the duration of the transaction.
     * If the object is already locked this method will wait until the lock is released.
     * A no wait option is available through setting the lock mode.
     * @see #refreshAndLockObject(Object, lockMode)
     */
    public Object refreshAndLockObject(Object object);

    /**
     * PUBLIC:
     * Refresh the attributes of the object and of all of its private parts from the database.
     * The object will be pessimisticly locked on the database for the duration of the transaction.
     * <p>Lock Modes: ObjectBuildingQuery.NO_LOCK, LOCK, LOCK_NOWAIT
     */
    public Object refreshAndLockObject(Object object, short lockMode);

    /**
     * PUBLIC:
     * Rollback the active database transaction.
     * This allows a group of database modification to be commited or rolledback as a unit.
     * All writes/deletes will be sent to the database be will not be visible to other users until commit.
     * Although databases do not allow nested transaction,
     * TopLink supports nesting through only committing to the database on the outer commit.
     *
     * @exception DatabaseException if the database connection is lost or the rollback fails.
     * @exception ConcurrencyException if this session is not within a transaction.
     */
    public void rollbackTransaction() throws DatabaseException;

    /**
     * PUBLIC:
     * Used for JTS integration.  If your application requires to have JTS control transactions instead of TopLink an
     * external transaction controler must be specified.  TopLink provides JTS controlers for JTS 1.0 and Weblogic's JTS.
     * @see oracle.toplink.essentials.transaction.JTATransactionController
     * @see oracle.toplink.essentials.platform.server.CustomServerPlatform
     */
    public void setExternalTransactionController(ExternalTransactionController etc);

    /**
     * PUBLIC:
     * Set the login.
     */
    public void setLogin(Login login);

    /**
     * PUBLIC:
     * Set the login.
     */
    public void setDatasourceLogin(Login login);

    /**
     * PUBLIC:
     * Update the object and all of its privately owned parts in the database.
     * Update should only be used if the application knows that the object is new,
     * otherwise writeObject should be used.
     * The update operation can be customized through using an update query.
     *
     * @see oracle.toplink.essentials.queryframework.UpdateObjectQuery
     * @see #writeObject(Object)
     */
    public Object updateObject(Object domainObject) throws DatabaseException, OptimisticLockException;

    /**
     * PUBLIC:
     * Write all of the objects and all of their privately owned parts in the database.
     * The allows for a group of objects to be commited as a unit.
     * The objects will be commited through a single transactions.
     *
     * @exception DatabaseException if an error occurs on the database,
     * these include constraint violations, security violations and general database erros.
     * @exception OptimisticLockException if the object's descriptor is using optimistic locking and
     * the object has been updated or deleted by another user since it was last read.
     */
    public void writeAllObjects(Collection domainObjects);

    /**
     * PUBLIC:
     * Write all of the objects and all of their privately owned parts in the database.
     * The allows for a group of objects to be commited as a unit.
     * The objects will be commited through a single transactions.
     *
     * @exception DatabaseException if an error occurs on the database,
     * these include constraint violations, security violations and general database erros.
     * @exception OptimisticLockException if the object's descriptor is using optimistic locking and
     * the object has been updated or deleted by another user since it was last read.
     */
    public void writeAllObjects(Vector domainObjects);

    /**
     * PUBLIC:
     * Write the object and all of its privately owned parts in the database.
     * Write will determine if an insert or an update should be done,
     * it may go to the database to determine this (by default will check the identity map).
     * The write operation can be customized through using an write query.
     *
     * @see oracle.toplink.essentials.queryframework.WriteObjectQuery
     * @see #insertObject(Object)
     * @see #updateObject(Object)
     */
    public Object writeObject(Object domainObject) throws DatabaseException, OptimisticLockException;
}
