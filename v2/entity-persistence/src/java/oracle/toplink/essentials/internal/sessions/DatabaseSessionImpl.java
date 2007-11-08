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
package oracle.toplink.essentials.internal.sessions;

import java.sql.Connection;
import java.sql.SQLException;

import java.util.*;

import oracle.toplink.essentials.descriptors.ClassDescriptor;
import oracle.toplink.essentials.exceptions.*;
import oracle.toplink.essentials.internal.helper.DBPlatformHelper;
import oracle.toplink.essentials.internal.databaseaccess.Platform;
import oracle.toplink.essentials.internal.databaseaccess.DatabasePlatform;
import oracle.toplink.essentials.internal.sequencing.Sequencing;
import oracle.toplink.essentials.internal.sequencing.SequencingCallback;
import oracle.toplink.essentials.internal.sequencing.SequencingHome;
import oracle.toplink.essentials.internal.sequencing.SequencingFactory;
import oracle.toplink.essentials.sequencing.SequencingControl;
import oracle.toplink.essentials.logging.SessionLog;
import oracle.toplink.essentials.sessions.Login;
import oracle.toplink.essentials.sessions.DatasourceLogin;
import oracle.toplink.essentials.sessions.SessionProfiler;
import oracle.toplink.essentials.platform.server.ServerPlatform;
import oracle.toplink.essentials.platform.server.NoServerPlatform;

/**
 * Implementation of oracle.toplink.essentials.sessions.DatabaseSession
 * The public interface should be used by public API, the implementation should be used internally.
 * @see oracle.toplink.essentials.sessions.DatabaseSession
 *
 * <p>
 * <b>Purpose</b>: Define the implementation for a single user/single connection TopLink session.
 * <p>
 * <b>Description</b>: The session is the primary interface into TopLink,
 * the application should do all of its reading and writing of objects through the session.
 * The session also manages transactions and units of work.  The database session is intended
 * for usage in two-tier client-server applications.  Although it could be used in a server
 * situation, it is limitted to only having a single database connection and only allows
 * a single open database transaction.
 * <p>
 * <b>Responsibilities</b>:
 *    <ul>
 *    <li> Connecting/disconnecting.
 *    <li> Reading and writing objects.
 *    <li> Transaction and unit of work support.
 *    <li> Identity maps and caching.
 *    </ul>
 */
public class DatabaseSessionImpl extends AbstractSession implements oracle.toplink.essentials.sessions.DatabaseSession {

    /**
     * INTERNAL:
     * sequencingHome for this session.
     */
    private SequencingHome sequencingHome;

    /**
     * Used to store the server platform that handles server-specific functionality for Oc4j, WLS,  etc.
     */
    private ServerPlatform serverPlatform;

    /**
     * INTERNAL:
     * connectedTime indicates the exact time this session was logged in.
     */
    private long connectedTime;

    /**
     * INTERNAL
     * Indicate if this session is logged in.
     */

    //Bug#3440544 Used to stop the attempt to login more than once. 
    protected boolean isLoggedIn;

    /**
     * INTERNAL:
     * Set the SequencingHome object used by the session.
     */
    protected void setSequencingHome(SequencingHome sequencingHome) {
        this.sequencingHome = sequencingHome;
    }

    /**
     * INTERNAL:
     * Return  SequencingHome which used to obtain all sequence-related
     * interfaces for DatabaseSession
     */
    protected SequencingHome getSequencingHome() {
        if (sequencingHome == null) {
            setSequencingHome(SequencingFactory.createSequencingHome(this));
        }
        return sequencingHome;
    }

    /**
     * PUBLIC:
     * Return  SequencingControl which used for sequencing setup and
     * customization including management of sequencing preallocation.
     */
    public SequencingControl getSequencingControl() {
        return getSequencingHome().getSequencingControl();
    }

    /**
     * INTERNAL:
     * Return the Sequencing object used by the session.
     */
    public Sequencing getSequencing() {
        return getSequencingHome().getSequencing();
    }

    /**
     * INTERNAL:
     * Creates sequencing object
     */
    public void initializeSequencing() {
        getSequencingHome().onDisconnect();
        getSequencingHome().onConnect();
    }

    /**
     * INTERNAL:
     * Called after transaction is completed (committed or rolled back)
     */
    public void afterTransaction(boolean committed, boolean isExternalTransaction) {
        SequencingCallback callback = getSequencingHome().getSequencingCallback();
        if (callback != null) {
            callback.afterTransaction(getAccessor(), committed);
        }
    }

    /**
     * INTERNAL:
     * Create and return a new default database session.
     * Used for EJB SessionManager to instantiate a database session
     */
    public DatabaseSessionImpl() {
        super();
        this.setServerPlatform(new NoServerPlatform(this));
    }

    /**
     * PUBLIC:
     * Create and return a new session.
     * By giving the login information on creation this allows the session to initialize itself
     * to the platform given in the login. This constructor does not return a connected session.
     * To connect the session to the database login() must be sent to it. The login(userName, password)
     * method may also be used to connect the session, this allows for the user name and password
     * to be given at login but for the other database information to be provided when the session is created.
     */
    public DatabaseSessionImpl(Login login) {
        this(new oracle.toplink.essentials.sessions.Project(login));
    }

    /**
     * PUBLIC:
     * Create and return a new session.
     * This constructor does not return a connected session.
     * To connect the session to the database login() must be sent to it. The login(userName, password)
     * method may also be used to connect the session, this allows for the user name and password
     * to be given at login but for the other database information to be provided when the session is created.
     */
    public DatabaseSessionImpl(oracle.toplink.essentials.sessions.Project project) {
        super(project);
        this.setServerPlatform(new NoServerPlatform(this));
    }

    /**
     * PUBLIC:
     * Add the descriptor to the session.
     * All persistent classes must have a descriptor registered for them with the session.
     * It is best to add the descriptors before login, if added after login the order in which
     * descriptors are added is dependant on inheritice and references unless the addDescriptors
     * method is used.
     *
     * @see #addDescriptors(Vector)
     * @see #addDescriptors(oracle.toplink.essentials.sessions.Project)
     */
    public void addDescriptor(ClassDescriptor descriptor) {
        // Reset cached data, as may be invalid later on.
        this.lastDescriptorAccessed = null;

        getProject().addDescriptor(descriptor, this);
    }

    /**
     * PUBLIC:
     * Add the descriptors to the session.
     * All persistent classes must have a descriptor registered for them with the session.
     * This method allows for a batch of descriptors to be added at once so that TopLink
     * can resolve the dependancies between the descriptors and perform initialization optimially.
     */
    public void addDescriptors(Vector descriptors) {
        // Reset cached data, as may be invalid later on.
        this.lastDescriptorAccessed = null;

        getProject().addDescriptors(descriptors, this);
    }

    /**
     * PUBLIC:
     * Add the descriptors to the session from the Project.
     * This can be used to combine the descriptors from multiple projects into a single session.
     * This can be called after the session has been connected as long as there are no external dependencies.
     */
    public void addDescriptors(oracle.toplink.essentials.sessions.Project project) {
        // Reset cached data, as may be invalid later on.
        this.lastDescriptorAccessed = null;

        getProject().addDescriptors(project, this);
    }

    /**
     * INTERNAL:
     * Connect the session only.
     */
    public void connect() throws DatabaseException {
        getAccessor().connect(getDatasourceLogin(), this);
    }

    /**
     * INTERNAL:
     * Disconnect the accessor only.
     */
    public void disconnect() throws DatabaseException {
        getSequencingHome().onDisconnect();
        getAccessor().disconnect(this);
    }

    /**
     * PUBLIC:
     * Answer the server platform to handle server specific behaviour for WLS, Oc4j, etc.
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
    public ServerPlatform getServerPlatform() {
        return serverPlatform;
    }

    /**
     * PUBLIC:
     * Set the server platform to handle server specific behaviour for WLS, Oc4j, etc
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
    public void setServerPlatform(ServerPlatform newServerPlatform) {
        if (this.isConnected()) {
            throw ValidationException.serverPlatformIsReadOnlyAfterLogin(newServerPlatform.getClass().getName());
        }
        this.serverPlatform = newServerPlatform;
    }

    /**
     * INTERNAL:
     * Logout in case still connected.
     */
    protected void finalize() throws DatabaseException {
        if (isConnected()) {
            logout();
        }
    }

    /**
     * PUBLIC:
     * Return all registered descriptors.
     */
    public Map getDescriptors() {
        return getProject().getDescriptors();
    }

    /**
     * INTERNAL:
     * Return the database platform currently connected to.
     * The platform is used for database specific behavior.
     * NOTE: this must only be used for relational specific usage,
     * it will fail for non-relational datasources.
     */
    public DatabasePlatform getPlatform() {
        // PERF: Cache the platform.
        if (platform == null) {
            if(isLoggedIn) {
                platform = getDatasourceLogin().getPlatform();
            } else {
                return getDatasourceLogin().getPlatform();
            }
        }
        return (DatabasePlatform)platform;
    }

    /**
     * INTERNAL:
     * Return the database platform currently connected to.
     * The platform is used for database specific behavior.
     */
    public Platform getDatasourcePlatform() {
        // PERF: Cache the platform.
        if (platform == null) {
            if(isLoggedIn) {
                platform = getDatasourceLogin().getDatasourcePlatform();
            } else {
                return getDatasourceLogin().getDatasourcePlatform();
            }
        }
        return platform;
    }

    /**
     * INTERNAL:
     * A descriptor may have been added after the session is logged in.
     * In this case the descriptor must be allowed to initialize any dependancies on this session.
     * Normally the descriptors are added before login, then initialized on login.
     */
    public void initializeDescriptorIfSessionAlive(ClassDescriptor descriptor) {
        if (isConnected() && (descriptor.requiresInitialization())) {
            try {
                try {
                    descriptor.preInitialize(this);
                    descriptor.initialize(this);
                    descriptor.postInitialize(this);
                    getCommitManager().initializeCommitOrder();
                } catch (RuntimeException exception) {
                    getIntegrityChecker().handleError(exception);
                }

                if (getIntegrityChecker().hasErrors()) {
                    //CR#4011
                    handleException(new IntegrityException(getIntegrityChecker()));
                }
            } finally {
                clearIntegrityChecker();
            }
        }
    }

    /**
     * INTERNAL:
     * Allow each descriptor to initialize any dependancies on this session.
     * This is done in two passes to allow the inheritence to be resolved first.
     * Normally the descriptors are added before login, then initialized on login.
     */
    public void initializeDescriptors() {
        // Assume all descriptors are CMP, if any are not their init will set this to false.
        getProject().setIsPureCMP2Project(true);
        // Must clone to avoid modification of the hashtable while enumerating.
        initializeDescriptors((Map)((HashMap)getDescriptors()).clone());
    }

    /**
     * INTERNAL:
     * Allow each descriptor to initialize any dependancies on this session.
     * This is done in two passes to allow the inheritence to be resolved first.
     * Normally the descriptors are added before login, then initialized on login.
     * The descriptors session must be used, not the broker.
     */
    public void initializeDescriptors(Map descriptors) {
        initializeSequencing();
        try {
            // First initialize basic properties (things that do not depend on anything else)
            Iterator iterator = descriptors.values().iterator();
            while (iterator.hasNext()) {
                ClassDescriptor descriptor = (ClassDescriptor)iterator.next();
                try {
                    AbstractSession session = getSessionForClass(descriptor.getJavaClass());
                    if (descriptor.requiresInitialization()) {
                        descriptor.preInitialize(session);
                    }

                    //check if inheritance is involved in aggregate relationship, and let the parent know the child descriptor
                    if (descriptor.isAggregateDescriptor() && descriptor.isChildDescriptor()) {
                        descriptor.initializeAggregateInheritancePolicy(session);
                    }
                } catch (RuntimeException exception) {
                    getIntegrityChecker().handleError(exception);
                }
            }

            // Second basic initialize mappings
            iterator = descriptors.values().iterator();
            while (iterator.hasNext()) {
                ClassDescriptor descriptor = (ClassDescriptor)iterator.next();
                try {
                    AbstractSession session = getSessionForClass(descriptor.getJavaClass());
                    if (descriptor.requiresInitialization()) {
                        descriptor.initialize(session);
                    }
                } catch (RuntimeException exception) {
                    getIntegrityChecker().handleError(exception);
                }
            }

            // Third initialize child dependencies
            iterator = descriptors.values().iterator();
            while (iterator.hasNext()) {
                ClassDescriptor descriptor = (ClassDescriptor)iterator.next();
                try {
                    AbstractSession session = getSessionForClass(descriptor.getJavaClass());
                    if (descriptor.requiresInitialization()) {
                        descriptor.postInitialize(session);
                    }
                } catch (RuntimeException exception) {
                    getIntegrityChecker().handleError(exception);
                }
            }

            try {
                getCommitManager().initializeCommitOrder();
            } catch (RuntimeException exception) {
                getIntegrityChecker().handleError(exception);
            }

            if (getIntegrityChecker().hasErrors()) {
                //CR#4011
                handleSevere(new IntegrityException(getIntegrityChecker()));
            }
        } finally {
            clearIntegrityChecker();
        }
    }

    /**
     * INTERNAL:
     * Allow each descriptor to initialize any dependancies on this session.
     * This is done in two passes to allow the inheritence to be resolved first.
     * Normally the descriptors are added before login, then initialized on login.
     * The descriptors session must be used, not the broker.
     */
    public void initializeDescriptors(Vector descriptors) {
        initializeSequencing();
        try {
            // First initialize basic properties (things that do not depend on anything else)
            for (Enumeration descriptorEnum = descriptors.elements();
                     descriptorEnum.hasMoreElements();) {
                try {
                    ClassDescriptor descriptor = (ClassDescriptor)descriptorEnum.nextElement();
                    AbstractSession session = getSessionForClass(descriptor.getJavaClass());
                    if (descriptor.requiresInitialization()) {
                        descriptor.preInitialize(session);
                    }

                    //check if inheritance is involved in aggregate relationship, and let the parent know the child descriptor
                    if (descriptor.isAggregateDescriptor() && descriptor.isChildDescriptor()) {
                        descriptor.initializeAggregateInheritancePolicy(session);
                    }
                } catch (RuntimeException exception) {
                    getIntegrityChecker().handleError(exception);
                }
            }

            // Second basic initialize mappings
            for (Enumeration descriptorEnum = descriptors.elements();
                     descriptorEnum.hasMoreElements();) {
                try {
                    ClassDescriptor descriptor = (ClassDescriptor)descriptorEnum.nextElement();
                    AbstractSession session = getSessionForClass(descriptor.getJavaClass());
                    if (descriptor.requiresInitialization()) {
                        descriptor.initialize(session);
                    }
                } catch (RuntimeException exception) {
                    getIntegrityChecker().handleError(exception);
                }
            }

            // Third initialize child dependencies
            for (Enumeration descriptorEnum = descriptors.elements();
                     descriptorEnum.hasMoreElements();) {
                try {
                    ClassDescriptor descriptor = (ClassDescriptor)descriptorEnum.nextElement();
                    AbstractSession session = getSessionForClass(descriptor.getJavaClass());
                    if (descriptor.requiresInitialization()) {
                        descriptor.postInitialize(session);
                    }
                } catch (RuntimeException exception) {
                    getIntegrityChecker().handleError(exception);
                }
            }

            try {
                getCommitManager().initializeCommitOrder();
            } catch (RuntimeException exception) {
                getIntegrityChecker().handleError(exception);
            }

            if (getIntegrityChecker().hasErrors()) {
                //CR#4011
                handleException(new IntegrityException(getIntegrityChecker()));
            }
        } finally {
            clearIntegrityChecker();
        }
    }

    /**
     * INTERNAL:
     * Return if this session is a database session.
     */
    public boolean isDatabaseSession() {
        return true;
    }

    /**
     * INTERNAL:
     * Return the login for the read connection.  Used by the platform autodetect feature
     */
    protected Login getReadLogin(){
        return getDatasourceLogin();
    }

    /**
     * PUBLIC:
     * Connect to the database using the predefined login.
     * Durring connection attempt to auto detect the required database platform.
     * This method can be used in systems where for ease of use developers have
     * TopLink autodetect the platform.
     * To be safe, however, the platform should be configured directly.
     * The login must have been assigned when or after creating the session.
     *
     */
    public void loginAndDetectDatasource() throws DatabaseException {
        preConnectDatasource();
        Connection conn = null;
        try{
            conn = (Connection)getReadLogin().connectToDatasource(null);
            getLogin().setPlatformClassName(DBPlatformHelper.getDBPlatform(conn.getMetaData().getDatabaseProductName(), getSessionLog()));
        }catch (SQLException ex){
            DatabaseException dbEx =  DatabaseException.errorRetrieveDbMetadataThroughJDBCConnection();
            // Typically exception would occur if user did not provide correct connection
            // parameters. The root cause of exception should be propogated up
            dbEx.initCause(ex);
            throw dbEx;
        }finally{
            if (conn != null){
                try{
                    conn.close();
                }catch (SQLException ex){
                    DatabaseException dbEx =  DatabaseException.errorRetrieveDbMetadataThroughJDBCConnection();
                    // Typically exception would occur if user did not provide correct connection
                    // parameters. The root cause of exception should be propogated up
                    dbEx.initCause(ex);
                    throw dbEx;
                }
            }
        }
        connect();
        postConnectDatasource();
    }

    /**
     * PUBLIC:
     * Connect to the database using the predefined login.
     * The login must have been assigned when or after creating the session.
     *
     * @see #login(Login)
     */
    public void login() throws DatabaseException {
        preConnectDatasource();
        connect();
        postConnectDatasource();
    }

    /**
     * PUBLIC:
     * Connect to the database using the given user name and password.
     * The additional login information must have been preset in the session's login attribute.
     * This is the login that should be used if each user has their own id,
     * but all users share the same database configuration.
     */
    public void login(String userName, String password) throws DatabaseException {
        getDatasourceLogin().setUserName(userName);
        getDatasourceLogin().setPassword(password);
        login();
    }

    /**
     * PUBLIC:
     * Connect to the database using the given login.
     * The login may also the preset and the login() protocol called.
     * This is the login should only be used if each user has their own database configuration.
     */
    public void login(Login login) throws DatabaseException {
        setLogin(login);
        login();
    }

    /**
     * PUBLIC:
     * Disconnect from the database.
     *
     * @exception TopLinkException if a transaction is active, you must rollback any active transaction before logout.
     * @exception DatabaseException the database will also raise an error if their is an active transaction,
     * or a general error occurs.
     */
    public void logout() throws DatabaseException {
        // Reset cached data, as may be invalid later on.
        this.lastDescriptorAccessed = null;

        if (isInTransaction()) {
            throw DatabaseException.logoutWhileTransactionInProgress();
        }
        if (getAccessor() == null) {
            return;
        }

        disconnect();
        getIdentityMapAccessor().initializeIdentityMaps();
        isLoggedIn = false;
        log(SessionLog.INFO, null, "logout_successful", this.getName());
	   
        //unregister the MBean
        getServerPlatform().unregisterMBean();
    }

    /**
     * PUBLIC:
     * Initialize the time that this session got connected. This can help determine how long a session has been
     * connected.
     */
    public void initializeConnectedTime() {
        connectedTime = System.currentTimeMillis();
    }

    /**
     * PUBLIC:
     * Answer the time that this session got connected. This can help determine how long a session has been
     * connected.
     */
    public long getConnectedTime() {
        return connectedTime;
    }

    /**
     * INTERNAL:
     * This method includes all of the code that is issued before the datasource
     * is connected to.
     */
    protected void preConnectDatasource(){
        //Bug#3440544 Check if logged in already to stop the attempt to login more than once
        if (isLoggedIn) {
            throw ValidationException.alreadyLoggedIn(this.getName());
        }
        this.platform = null;
        if (isInProfile()) {
            getProfiler().initialize();
        }
        updateProfile(SessionProfiler.LoginTime, new Date(System.currentTimeMillis()));

        // Login and initialize
        getEventManager().preLogin(this);
        //setup the external transaction controller
        getServerPlatform().initializeExternalTransactionController();
        log(SessionLog.INFO, null, "topLink_version", DatasourceLogin.getVersion());
        if (getServerPlatform().getServerNameAndVersion() != null) {
            log(SessionLog.INFO, null, "application_server_name_and_version", getServerPlatform().getServerNameAndVersion());
        }
    }

    /**
     * INTERNAL:
     * This method includes all of the code that is issued after the datasource
     * is connected to.
     */
    protected void postConnectDatasource(){
        initializeDescriptors();
        //added to process ejbQL query strings
        processEJBQLQueries();
        log(SessionLog.INFO, null, "login_successful", this.getName());
        getEventManager().postLogin(this);

        initializeConnectedTime();
        this.isLoggedIn = true;
        this.platform = null;
        
        //register the MBean
        getServerPlatform().registerMBean();
    }

    /**
     * PUBLIC:
     * Write all of the objects and all of their privately owned parts in the database.
     * The objects will be commited through a single transaction.
     *
     * @exception DatabaseException if an error occurs on the database,
     * these include constraint violations, security violations and general database erros.
     * @exception OptimisticLockException if the object's descriptor is using optimistic locking and
     * the object has been updated or deleted by another user since it was last read.
     */
    public void writeAllObjects(Collection domainObjects) throws DatabaseException, OptimisticLockException {
        for (Iterator objectsEnum = domainObjects.iterator(); objectsEnum.hasNext();) {
            writeObject(objectsEnum.next());
        }
    }

    /**
     * PUBLIC:
     * Write all of the objects and all of their privately owned parts in the database.
     * The objects will be commited through a single transaction.
     *
     * @exception DatabaseException if an error occurs on the database,
     * these include constraint violations, security violations and general database erros.
     * @exception OptimisticLockException if the object's descriptor is using optimistic locking and
     * the object has been updated or deleted by another user since it was last read.
     */
    public void writeAllObjects(Vector domainObjects) throws DatabaseException, OptimisticLockException {
        for (Enumeration objectsEnum = domainObjects.elements(); objectsEnum.hasMoreElements();) {
            writeObject(objectsEnum.nextElement());
        }
    }
}
